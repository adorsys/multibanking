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
import de.adorsys.multibanking.domain.Credentials;
import de.adorsys.multibanking.domain.Product;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.exception.ScaRequiredException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
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

import java.util.List;
import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.*;
import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.startHbciDialog;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class ScaRequiredJob<T extends AbstractResponse> {

    public T execute(HbciCallback hbciCallback) {
        return authorisationAwareExecute(hbciCallback);
    }

    private T authorisationAwareExecute(HbciCallback hbciCallback) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        AuthorisationCodeResponse authorisationCodeResponse =
            AuthorisationCodeResponse.builder().tanSubmit(hbciTanSubmit).build();

        HBCIDialog dialog = createAndStartDialog(hbciCallback, authorisationCodeResponse);

        //could be null in case of empty hktan requests
        List<AbstractHBCIJob> hbciJobs = createHbciJobs(dialog.getPassport());

        //hbciJobs could be null in case of tan request without corresponding hbci request (TAN verbrennen)
        boolean tan2StepRequired = hbciJobs == null || dialog.getPassport().tan2StepRequired(hbciJobs);

        if (tan2StepRequired) {
            requestAuthorisationCode(hbciTanSubmit, dialog, hbciJobs);
        } else {
            //No SCA needed
            dialog.addTasks(hbciJobs);
        }

        execute(dialog, !tan2StepRequired);

        if (tan2StepRequired && authorisationCodeResponse.getChallenge() != null) {
            updateTanSubmit(hbciTanSubmit, dialog, hbciJobs != null ? hbciJobs.get(0) : null);
            throw new ScaRequiredException(authorisationCodeResponse);
        }

        return createJobResponse(dialog.getPassport(), authorisationCodeResponse);
    }

    private void requestAuthorisationCode(HbciTanSubmit hbciTanSubmit, HBCIDialog dialog,
                                          List<AbstractHBCIJob> hbciJobs) {
        HBCITwoStepMechanism hbciTwoStepMechanism = getUserTanTransportType(dialog);
        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        if (hbciTwoStepMechanism.getProcess() == 1 && hbciJobs == null) {
            throw new MultibankingException(INTERNAL_ERROR, "Tan requests without corresponding transaction not " +
                "supported with HKTAN process variant 1");
        }

        if (hbciJobs != null) {
            hbciJobs.forEach(hbciJob -> {
                if (hbciTwoStepMechanism.getProcess() == 2) {
                    hktanProcess2(dialog, hbciTwoStepMechanism, hbciJob);
                } else {
                    hktanProcess1(dialog, hbciTwoStepMechanism, hbciTanSubmit, hbciJob);
                }
            });
        } else {
            hktanProcess2(dialog, hbciTwoStepMechanism, null);
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
                hbciTanSubmit.setHbciJobName(hbciJob.getHBCICode());
            });
    }

    private HBCIDialog createAndStartDialog(HbciCallback hbciCallback,
                                            AuthorisationCodeResponse authorisationCodeResponse) {
        HbciCallback callback = createCallback(hbciCallback, authorisationCodeResponse);
        HbciDialogRequest dialogRequest = createDialogRequest(callback);
        return startHbciDialog(null, dialogRequest);
    }

    void execute(HBCIDialog dialog, boolean closeDialog) {
        //TODO throw HbciAuthorisationRequiredException in case of needed SCA
        HBCIExecStatus status = dialog.execute(closeDialog);
        if (!status.isOK()) {
            throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages());
        }
    }

    private void hktanProcess1(HBCIDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism,
                               HbciTanSubmit hbciTanSubmit,
                               AbstractHBCIJob hbciJob) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), hbciJob);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("process", hbciTwoStepMechanism.getProcess());
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", hbciJob.createOrderHash(hbciTwoStepMechanism.getSegversion()));

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(hbciJob, hktan, hbciTwoStepMechanism);
        }

        hbciTanSubmit.setSepaPain(hbciJob.getRawData());

        dialog.addTask(hktan, false);
    }

    private void hktanProcess2(HBCIDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism, AbstractHBCIJob hbciJob) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), hbciJob);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        Konto psuKonto = getPsuKonto(dialog.getPassport());

        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("ordersegcode", "HKIDN");
        hktan.setParam("orderaccount", psuKonto);

        Optional<List<AbstractHBCIJob>> messages = Optional.ofNullable(hbciJob)
            .map(dialog::addTask);

        if (messages.isPresent()) {
            messages.get().add(hktan);
        } else {
            dialog.addTask(hktan);
        }
    }

    Konto getPsuKonto(PinTanPassport passport) {
        BankAccount account = getPsuBankAccount();
        Konto konto = passport.findAccountByAccountNumber(Iban.valueOf(account.getIban()).getAccountNumber());
        konto.iban = account.getIban();
        konto.bic = account.getBic();
        return konto;
    }

    BankAccount getPsuBankAccount() {
        return Optional.ofNullable(getTransactionRequest().getTransaction().getPsuAccount())
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE, "Missing transaction psu account"));
    }

    HBCITwoStepMechanism getUserTanTransportType(HBCIDialog dialog) {
        return Optional.of(getConsent().getSelectedMethod())
            .map(tanTransportType -> dialog.getPassport().getBankTwostepMechanisms().get(tanTransportType.getId()))
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

                    Optional.ofNullable(challengeHhdUc)
                        .ifPresent(imageData -> {
                            try {
                                byte[] image = new MatrixCode(imageData).getImage();
                                challengeData.setImage(new String(image, UTF_8));
                            } catch (Exception e) {
                                challengeData.setData(imageData);
                            }
                        });
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
        Credentials credentials = Optional.ofNullable(getConsent().getCredentials())
            .orElse(getTransactionRequest().getCredentials());

        HbciDialogRequest hbciDialogRequest = HbciDialogRequest.builder()
            .credentials(credentials)
            .hbciPassportState(transactionRequest.getBankAccess().getHbciPassportState())
            .callback(hbciCallback)
            .build();

        hbciDialogRequest.setBankCode(transactionRequest.getBankCode() != null ? transactionRequest.getBankCode() :
            transactionRequest.getBankAccess().getBankCode());
        hbciDialogRequest.setHbciProduct(Optional.ofNullable(transactionRequest.getHbciProduct())
            .map(product -> new Product(product.getName(), product.getVersion()))
            .orElse(null));
        hbciDialogRequest.setHbciBPD(transactionRequest.getHbciBPD());
        hbciDialogRequest.setHbciUPD(transactionRequest.getHbciUPD());
        hbciDialogRequest.setHbciSysId(transactionRequest.getHbciSysId());

        return hbciDialogRequest;
    }

    HBCIConsent getConsent() {
        return (HBCIConsent) getTransactionRequest().getBankApiConsentData();
    }

    abstract TransactionRequest getTransactionRequest();

    abstract String getHbciJobName(AbstractScaTransaction.TransactionType transactionType);

    abstract T createJobResponse(PinTanPassport passport, AuthorisationCodeResponse response);

    public abstract List<AbstractHBCIJob> createHbciJobs(PinTanPassport passport);

    public abstract String orderIdFromJobResult(HBCIJobResult jobResult);

}
