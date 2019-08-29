/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.ChallengeData;
import de.adorsys.multibanking.domain.Product;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.hbci.model.*;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.Iban;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.*;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;
import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;
import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.startHbciDialog;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class ScaRequiredJob<T extends AbstractScaTransaction, R extends AbstractResponse> {

    static HbciObjectMapper hbciObjectMapper = new HbciObjectMapperImpl();

    public R authorisationAwareExecute(HbciCallback hbciCallback) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        AuthorisationCodeResponse authorisationCodeResponse =
            AuthorisationCodeResponse.builder().tanSubmit(hbciTanSubmit).build();

        HBCIDialog dialog = createAndStartDialog(hbciCallback, authorisationCodeResponse);

        //could be null in case of empty hktan requests
        AbstractHBCIJob hbciJob = createScaMessage(dialog.getPassport());

        //hbciJobs could be null in case of tan request without corresponding hbci request (TAN verbrennen)
        boolean tan2StepRequired = hbciJob == null || dialog.getPassport().tan2StepRequired(hbciJob);

        GVTAN2Step hktan = null;
        if (tan2StepRequired) {
            hktan = requestAuthorisationCode(hbciTanSubmit, dialog, hbciJob);
        } else {
            //No SCA needed
            dialog.addTask(hbciJob);
        }

        execute(dialog, !tan2StepRequired);

        //check for SCA is really needed after execution
        tan2StepRequired = Optional.ofNullable(hktan)
            .map(gvtan2Step -> KnownReturncode.W3076.searchReturnValue(gvtan2Step.getJobResult().getJobStatus().getRetVals()) == null)
            .orElse(false);

        R jobResponse = createJobResponse(dialog.getPassport(), null);
        if (tan2StepRequired) {
            updateTanSubmit(hbciTanSubmit, dialog, hbciJob);
            jobResponse.setAuthorisationCodeResponse(authorisationCodeResponse);
            return jobResponse;
        } else {
            dialog.close();
            return jobResponse;
        }
    }

    private GVTAN2Step requestAuthorisationCode(HbciTanSubmit hbciTanSubmit, HBCIDialog dialog,
                                                AbstractHBCIJob hbciJob) {
        HBCITwoStepMechanism hbciTwoStepMechanism = getUserTanTransportType(dialog);
        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        if (hbciTwoStepMechanism.getProcess() == 1 && hbciJob == null) {
            throw new MultibankingException(INTERNAL_ERROR, "Tan requests without corresponding transaction not " +
                "supported with HKTAN process variant 1");
        }

        if (hbciJob == null || hbciTwoStepMechanism.getProcess() == 2) {
            return hktanProcess2(dialog, hbciTwoStepMechanism, hbciJob);
        } else {
            return hktanProcess1(dialog, hbciTwoStepMechanism, hbciTanSubmit, hbciJob);
        }

    }

    private void updateTanSubmit(HbciTanSubmit hbciTanSubmit, HBCIDialog dialog, AbstractHBCIJob scaJob) {
        hbciTanSubmit.setOriginJobName(
            Optional.ofNullable(getTransactionRequest().getTransaction())
                .map(transaction -> getHbciJobName(transaction.getTransactionType()))
                .orElseGet(() -> getHbciJobName(null)));

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setTwoStepMechanism(getUserTanTransportType(dialog));
        Optional.ofNullable(scaJob)
            .ifPresent(hbciJob -> {
                Optional.ofNullable(hbciJob.getPainVersion())
                    .ifPresent(painVersion -> hbciTanSubmit.setPainVersion(painVersion.getURN()));
                hbciTanSubmit.setOriginLowLevelName(hbciJob.getJobName());
                hbciTanSubmit.setOriginSegVersion(hbciJob.getSegVersion());
                hbciTanSubmit.setHbciJobName(hbciJob.getHBCICode(false));
            });
    }

    private HBCIDialog createAndStartDialog(HbciCallback hbciCallback,
                                            AuthorisationCodeResponse authorisationCodeResponse) {
        HbciCallback callback = createCallback(hbciCallback, authorisationCodeResponse);
        HbciDialogRequest dialogRequest = createDialogRequest(callback);
        return startHbciDialog(null, dialogRequest);
    }

    void execute(HBCIDialog dialog, boolean closeDialog) {
        HBCIExecStatus status = dialog.execute(closeDialog);
        if (!status.isOK()) {
            throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages()
                .stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }
    }

    private GVTAN2Step hktanProcess1(HBCIDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism,
                                     HbciTanSubmit hbciTanSubmit,
                                     AbstractHBCIJob hbciJob) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), hbciJob);
        hktan.setProcess(KnownTANProcess.PROCESS1);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", hbciJob.createOrderHash(hbciTwoStepMechanism.getSegversion()));

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(hbciJob, hktan, hbciTwoStepMechanism);
        }

        hbciTanSubmit.setSepaPain(hbciJob.getRawData());

        dialog.addTask(hktan, false);
        return hktan;
    }

    private GVTAN2Step hktanProcess2(HBCIDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism,
                                     AbstractHBCIJob hbciJob) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), hbciJob);
        hktan.setProcess(KnownTANProcess.PROCESS2_STEP1);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("orderaccount", getPsuKonto(dialog.getPassport()));
        hktan.setParam("ordersegcode", Optional.ofNullable(hbciJob)
            .map(AbstractHBCIJob::getHBCICode)
            .orElse("HKIDN"));

        Optional<HBCIMessage> messages = Optional.ofNullable(hbciJob)
            .map(dialog::addTask);

        if (messages.isPresent()) {
            messages.get().append(hktan);
        } else {
            dialog.addTask(hktan);
        }

        return hktan;
    }

    Konto getPsuKonto(PinTanPassport passport) {
        BankAccount account = getPsuBankAccount();
        Konto konto = passport.findAccountByAccountNumber(Iban.valueOf(account.getIban()).getAccountNumber());
        konto.iban = account.getIban();
        konto.bic = Optional.ofNullable(account.getBic()).orElse(getTransactionRequest().getBank().getBic());
        return konto;
    }

    BankAccount getPsuBankAccount() {
        return Optional.ofNullable(getTransactionRequest().getTransaction().getPsuAccount())
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE, "Missing transaction psu account"));
    }

    private HBCITwoStepMechanism getUserTanTransportType(HBCIDialog dialog) {
        return Optional.of(getConsent().getSelectedMethod())
            .map(tanTransportType -> dialog.getPassport().getBankTwostepMechanisms().get(tanTransportType.getId()))
            .map(hbciTwoStepMechanism -> {
                hbciTwoStepMechanism.setMedium(getConsent().getSelectedMethod().getMedium());
                return hbciTwoStepMechanism;
            })
            .orElseThrow(() -> new MultibankingException(INVALID_SCA_METHOD));
    }

    private HbciCallback createCallback(HbciCallback hbciCallback, AuthorisationCodeResponse response) {
        return new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge, String challengeHhdUc,
                                             HHDVersion.Type type) {
                //needed later for submitAuthorizationCode
                ((HbciTanSubmit) response.getTanSubmit()).setOrderRef(orderRef);
                if (challenge != null) {
                    ChallengeData challengeData = new ChallengeData();
                    challengeData.setAdditionalInformation(challenge);

                    if (challengeHhdUc != null) {
                        try {
                            byte[] image = new MatrixCode(challengeHhdUc).getImage();
                            challengeData.setImage(new String(image, UTF_8));
                        } catch (Exception e) {
                            challengeData.setData(Collections.singletonList(challengeHhdUc));
                        }
                    }

                    UpdateAuthResponse updateAuthResponse = new UpdateAuthResponse();
                    updateAuthResponse.setBankApi(HBCI);
                    updateAuthResponse.setScaStatus(SCAMETHODSELECTED);
                    updateAuthResponse.setScaApproach(EMBEDDED);
                    updateAuthResponse.setChallenge(challengeData);

                    response.setUpdateAuthResponse(updateAuthResponse);
                }
            }

            @Override
            public void status(int statusTag, Object o) {
                Optional.ofNullable(hbciCallback)
                    .ifPresent(callback -> callback.status(statusTag, o));
            }

            @Override
            public void status(int statusTag, Object[] o) {
                Optional.ofNullable(hbciCallback)
                    .ifPresent(callback -> callback.status(statusTag, o));
            }
        };
    }

    private HbciDialogRequest createDialogRequest(HbciCallback hbciCallback) {
        TransactionRequest transactionRequest = getTransactionRequest();

        HbciDialogRequest hbciDialogRequest = HbciDialogRequest.builder()
            .credentials(getConsent().getCredentials())
            .hbciPassportState(transactionRequest.getBankAccess().getHbciPassportState())
            .callback(hbciCallback)
            .build();

        hbciDialogRequest.setBank(transactionRequest.getBank());
        hbciDialogRequest.setHbciProduct(Optional.ofNullable(transactionRequest.getHbciProduct())
            .map(product -> new Product(product.getName(), product.getVersion()))
            .orElse(null));
        hbciDialogRequest.setHbciBPD(transactionRequest.getHbciBPD());
        hbciDialogRequest.setHbciUPD(transactionRequest.getHbciUPD());
        hbciDialogRequest.setHbciSysId(transactionRequest.getHbciSysId());

        return hbciDialogRequest;
    }

    private HBCIConsent getConsent() {
        return (HBCIConsent) getTransactionRequest().getBankApiConsentData();
    }

    public abstract AbstractHBCIJob createScaMessage(PinTanPassport passport);

    public abstract List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport);

    abstract TransactionRequest<T> getTransactionRequest();

    abstract String getHbciJobName(AbstractScaTransaction.TransactionType transactionType);

    abstract R createJobResponse(PinTanPassport passport, AbstractHBCIJob hbciJob);

    public abstract String orderIdFromJobResult(HBCIJobResult jobResult);

}
