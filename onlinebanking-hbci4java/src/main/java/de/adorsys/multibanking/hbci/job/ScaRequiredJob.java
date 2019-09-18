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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.ChallengeData;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.hbci.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.Iban;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.manager.*;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.status.HBCIMsgStatus;
import org.kapott.hbci.structures.Konto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;
import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.bpd;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.jobs;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public abstract class ScaRequiredJob<T extends AbstractScaTransaction, R extends AbstractResponse> {

    static HbciObjectMapper hbciObjectMapper = new HbciObjectMapperImpl();

    public R authorisationAwareExecute(HBCICallback hbciCallback) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        AuthorisationCodeResponse authorisationCodeResponse = new AuthorisationCodeResponse(hbciTanSubmit);

        PinTanPassport bpdPassport = fetchBpd(hbciCallback);

        HBCIJobsDialog dialog = (HBCIJobsDialog) createDialog(jobs, hbciCallback, authorisationCodeResponse,
            getUserTanTransportType(bpdPassport.getBankTwostepMechanisms()));
        dialog.getPassport().setBPD(bpdPassport.getBPD());

        HBCIMsgStatus dialogInitMsgStatus = dialog.dialogInit(true);

        //could be null in case of empty hktan requests
        AbstractHBCIJob hbciJob = createJobMessage(dialog.getPassport());
        if (checkDialogInitScaRequired(dialogInitMsgStatus, dialog, hbciTanSubmit, hbciJob)) {
            R jobResponse = createJobResponse(dialog.getPassport());
            jobResponse.setAuthorisationCodeResponse(authorisationCodeResponse);
            return jobResponse;
        }

        //hbciJobs could be null in case of tan request without corresponding hbci request (TAN verbrennen)
        boolean tan2StepRequired = hbciJob == null || dialog.getPassport().tan2StepRequired(hbciJob);

        GVTAN2Step hktan = null;
        if (tan2StepRequired) {
            hktan = prepareHbciMessagefor2FA(hbciTanSubmit, dialog, hbciJob);
        } else {
            //No SCA needed
            dialog.addTask(hbciJob);
        }

        executeTasks(dialog);

        //check for SCA is really needed after execution
        tan2StepRequired = Optional.ofNullable(hktan)
            .map(gvtan2Step -> KnownReturncode.W3076.searchReturnValue(gvtan2Step.getJobResult().getJobStatus().getRetVals()) == null
                && KnownReturncode.W3076.searchReturnValue(gvtan2Step.getJobResult().getGlobStatus().getRetVals()) == null)
            .orElse(false);

        R jobResponse = createJobResponse(dialog.getPassport());
        if (tan2StepRequired) {
            updateTanSubmit(hbciTanSubmit, dialog, hbciJob);
            jobResponse.setAuthorisationCodeResponse(authorisationCodeResponse);
        }

        //sca not needed and dialog not closed
        if (!tan2StepRequired) {
            dialog.close();
        }

        return jobResponse;
    }

    private boolean checkDialogInitScaRequired(HBCIMsgStatus initMsgStatus, AbstractHbciDialog dialog,
                                               HbciTanSubmit hbciTanSubmit, AbstractHBCIJob hbciJob) {
        if (!initMsgStatus.isOK()) {
            throw new MultibankingException(HBCI_ERROR, initMsgStatus.getErrorList()
                .stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }

        boolean scaRequired = initMsgStatus.segStatus.getRetVals().stream()
            .anyMatch(hbciRetVal -> hbciRetVal.code.equals("0030"));

        if (scaRequired) {
            updateTanSubmit(hbciTanSubmit, dialog, hbciJob);
            hbciTanSubmit.setHbciJobName("HKIDN");

            String header = "TAN2StepRes" + hbciTanSubmit.getTwoStepMechanism().getSegversion();
            dialog.getPassport().getCallback().tanChallengeCallback(initMsgStatus.getData().get(header + ".orderref")
                , initMsgStatus.getData().get(header + ".challenge"), initMsgStatus.getData().get(header +
                    ".challenge_hhd_uc"), null);
        }

        return scaRequired;
    }

    private PinTanPassport fetchBpd(HBCICallback hbciCallback) {
        AbstractHbciDialog dialog = createDialog(bpd, hbciCallback, null, null);
        dialog.execute(true);
        return dialog.getPassport();
    }

    private GVTAN2Step prepareHbciMessagefor2FA(HbciTanSubmit hbciTanSubmit, HBCIJobsDialog dialog,
                                                AbstractHBCIJob hbciJob) {
        HBCITwoStepMechanism hbciTwoStepMechanism =
            getUserTanTransportType(dialog.getPassport().getBankTwostepMechanisms());

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

    private void updateTanSubmit(HbciTanSubmit hbciTanSubmit, AbstractHbciDialog dialog, AbstractHBCIJob scaJob) {
        hbciTanSubmit.setOriginJobName(
            Optional.ofNullable(getTransactionRequest().getTransaction())
                .map(transaction -> getHbciJobName(transaction.getTransactionType()))
                .orElseGet(() -> getHbciJobName(null)));

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogId());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setTwoStepMechanism(getUserTanTransportType(dialog.getPassport().getBankTwostepMechanisms()));

        Optional.ofNullable(scaJob)
            .ifPresent(hbciJob -> {
                try {
                    hbciTanSubmit.setLowLevelParams(new ObjectMapper().writeValueAsString(scaJob.getLowlevelParams()));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new MultibankingException(INTERNAL_ERROR, 500, e.getMessage());
                }

                Optional.ofNullable(hbciJob.getPainVersion())
                    .ifPresent(painVersion -> hbciTanSubmit.setPainVersion(painVersion.getURN()));
                hbciTanSubmit.setOriginLowLevelName(hbciJob.getJobName());
                hbciTanSubmit.setOriginSegVersion(hbciJob.getSegVersion());
                hbciTanSubmit.setHbciJobName(hbciJob.getHBCICode(false));
            });
    }

    private AbstractHbciDialog createDialog(HbciDialogType dialogType, HBCICallback hbciCallback,
                                            AuthorisationCodeResponse authorisationCodeResponse,
                                            HBCITwoStepMechanism twoStepMechanism) {
        HBCICallback callback = createCallback(hbciCallback, authorisationCodeResponse);
        HbciDialogRequest dialogRequest = createDialogRequest(callback);

        return HbciDialogFactory.createDialog(dialogType, null, dialogRequest, twoStepMechanism);
    }

    private void executeTasks(AbstractHbciDialog dialog) {
        HBCIExecStatus execStatus = dialog.execute(false);
        if (!execStatus.isOK()) {
            throw new MultibankingException(HBCI_ERROR, execStatus.getErrorMessages()
                .stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }
    }

    private GVTAN2Step hktanProcess1(HBCIJobsDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism,
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

    private GVTAN2Step hktanProcess2(HBCIJobsDialog dialog, HBCITwoStepMechanism hbciTwoStepMechanism,
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
        Optional.ofNullable(hbciJob)
            .map(AbstractHBCIJob::getHBCICode)
            .ifPresent(hbciCode -> hktan.setParam("ordersegcode", hbciCode));

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
        return getPsuAccount()
            .map(account -> {
                Konto konto = passport.findAccountByAccountNumber(Iban.valueOf(account.getIban()).getAccountNumber());
                konto.iban = account.getIban();
                konto.bic = Optional.ofNullable(account.getBic())
                    .orElse(HBCIUtils.getBankInfo(konto.blz).getBic());
                return konto;
            })
            .orElseGet(() -> passport.getAccounts().get(0));
    }

    private Optional<BankAccount> getPsuAccount() {
        return Optional.ofNullable(getTransactionRequest().getTransaction().getPsuAccount());
    }

    private HBCITwoStepMechanism getUserTanTransportType(Map<String, HBCITwoStepMechanism> bpdScaMethods) {
        return Optional.ofNullable(getConsent().getSelectedMethod())
            .map(tanTransportType -> bpdScaMethods.get(tanTransportType.getId()))
            .map(hbciTwoStepMechanism -> {
                hbciTwoStepMechanism.setMedium(getConsent().getSelectedMethod().getMedium());
                return hbciTwoStepMechanism;
            })
            .orElseGet(() -> {
                HBCITwoStepMechanism hbciTwoStepMechanism = new HBCITwoStepMechanism();
                hbciTwoStepMechanism.setSecfunc("999");
                hbciTwoStepMechanism.setSegversion(6);
                hbciTwoStepMechanism.setProcess(2);
                hbciTwoStepMechanism.setId("999");
                return hbciTwoStepMechanism;
            });
    }

    private HBCICallback createCallback(HBCICallback hbciCallback, AuthorisationCodeResponse response) {
        return new AbstractHBCICallback()  {

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

    private HbciDialogRequest createDialogRequest(HBCICallback hbciCallback) {
        return hbciObjectMapper.toHbciDialogRequest(getTransactionRequest(), hbciCallback);
    }

    private HbciConsent getConsent() {
        return (HbciConsent) getTransactionRequest().getBankApiConsentData();
    }

    public abstract AbstractHBCIJob createJobMessage(PinTanPassport passport);

    public abstract List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport);

    abstract TransactionRequest<T> getTransactionRequest();

    abstract String getHbciJobName(AbstractScaTransaction.TransactionType transactionType);

    abstract R createJobResponse(PinTanPassport passport);

    public abstract String orderIdFromJobResult(HBCIJobResult jobResult);

}
