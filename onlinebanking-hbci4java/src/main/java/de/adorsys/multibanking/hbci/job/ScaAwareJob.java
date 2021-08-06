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
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import de.adorsys.multibanking.hbci.model.*;
import de.adorsys.multibanking.hbci.util.HbciErrorUtils;
import de.adorsys.multibanking.mapper.AccountStatementMapper;
import de.adorsys.multibanking.mapper.AccountStatementMapperImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.Iban;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV.GVVeuStep;
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
import java.util.Map;
import java.util.Optional;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;
import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.BPD;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.JOBS;

@RequiredArgsConstructor
@Slf4j
public abstract class ScaAwareJob<T extends AbstractTransaction, R extends AbstractResponse> {

    private static final HbciDialogRequestMapper hbciDialogRequestMapper = new HbciDialogRequestMapperImpl();
    static AccountStatementMapper accountStatementMapper = new AccountStatementMapperImpl();
    final TransactionRequest<T> transactionRequest;
    @Getter
    private final HbciBpdCacheHolder hbciBpdCacheHolder;

    HBCIJobsDialog dialog;

    AbstractHBCIJob hbciJob;

    HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
    private UpdateAuthResponse challenge;

    public R execute(HBCICallback hbciCallback) {
        if (this.dialog == null) {
            R jobResponse = initDialog(hbciCallback);
            if (jobResponse != null) return jobResponse; //TAN needed for HKIDN
        }

        //could be null in case of empty hktan requests
        AbstractHBCIJob newHbciJob = getOrCreateHbciJob();

        //hbciJob could be null in case of tan request without corresponding hbci request (TAN verbrennen)
        boolean tan2StepRequired = newHbciJob == null || dialog.getPassport().tan2StepRequired(newHbciJob);

        GVTAN2Step hktan = null;
        if (tan2StepRequired) {
            hktan = prepareHbciMessagefor2FA();
        } else {
            //No SCA needed
            dialog.addTask(newHbciJob);
        }

        HBCIExecStatus hbciExecStatus = dialog.execute(false);
        checkExecuteStatus(hbciExecStatus);

        //check for SCA is really needed after execution
        tan2StepRequired = Optional.ofNullable(hktan)
            .map(gvtan2Step -> KnownReturncode.W3076.searchReturnValue(gvtan2Step.getJobResult().getJobStatus().getRetVals()) == null
                && KnownReturncode.W3076.searchReturnValue(gvtan2Step.getJobResult().getGlobStatus().getRetVals()) == null)
            .orElse(false);

        R jobResponse = createJobResponse();
        jobResponse.setMessages(HbciErrorUtils.msgStatusListToMessages(hbciExecStatus.getMsgStatusList()));

        if (tan2StepRequired) {
            hbciTanSubmit.update(dialog, newHbciJob, getHbciJobName(),
                getUserTanTransportType(dialog.getPassport().getBankTwostepMechanisms()), getHbciKonto());
            jobResponse.setAuthorisationCodeResponse(new AuthorisationCodeResponse(hbciTanSubmit, challenge));
        } else if (getConsent().isCloseDialog()) { //sca not needed
            dialog.dialogEnd();
        }

        return jobResponse;
    }

    private R initDialog(HBCICallback hbciCallback) {
        log.debug("init new hbci dialog");
        PinTanPassport bpdPassport = fetchBpd(hbciCallback);

        dialog = (HBCIJobsDialog) createDialog(JOBS, hbciCallback, getUserTanTransportType(bpdPassport.getBankTwostepMechanisms()), bpdPassport.getBPD());

        HBCIMsgStatus dialogInitMsgStatus = dialog.dialogInit(getConsent().isWithHktan());

        if (checkDialogInitScaRequired(dialogInitMsgStatus)) {
            log.debug("HKIDN SCA required");
            R jobResponse = createJobResponse();
            jobResponse.setAuthorisationCodeResponse(new AuthorisationCodeResponse(hbciTanSubmit, challenge));
            return jobResponse;
        }
        return null;
    }

    private boolean checkDialogInitScaRequired(HBCIMsgStatus initMsgStatus) {
        if (!initMsgStatus.isOK()) {
            throw HbciErrorUtils.toMultibankingException(Collections.singletonList(initMsgStatus));
        }

        boolean scaRequired = initMsgStatus.segStatus.getRetVals().stream()
            .anyMatch(hbciRetVal -> hbciRetVal.code.equals("0030"));

        if (scaRequired) {
            HBCITwoStepMechanism userTanTransportType = getUserTanTransportType(dialog.getPassport().getBankTwostepMechanisms());
            hbciTanSubmit.update(dialog, getOrCreateHbciJob(), getHbciJobName(), userTanTransportType, getHbciKonto());
            hbciTanSubmit.setHbciJobName("HKIDN"); //overwrite hbci job name for second HKTAN request

            String header = "TAN2StepRes" + userTanTransportType.getSegversion();
            dialog.getPassport().getCallback().tanChallengeCallback(initMsgStatus.getData().get(header + ".orderref"),
                initMsgStatus.getData().get(header + ".challenge"), initMsgStatus.getData().get(header + ".challenge_hhd_uc"), null);
        }

        return scaRequired;
    }

    PinTanPassport fetchBpd(HBCICallback hbciCallback) {
        AbstractHbciDialog bpdDialog = createDialog(BPD, hbciCallback, null, null);
        bpdDialog.execute(true);
        return bpdDialog.getPassport();
    }

    private GVTAN2Step prepareHbciMessagefor2FA() {
        HBCITwoStepMechanism hbciTwoStepMechanism =
            getUserTanTransportType(dialog.getPassport().getBankTwostepMechanisms());

        if (hbciTwoStepMechanism.getProcess() == 1 && getOrCreateHbciJob() == null) {
            throw new MultibankingException(INTERNAL_ERROR, "Tan requests without corresponding transaction not " +
                "supported with HKTAN process variant 1");
        }

        if (getOrCreateHbciJob() == null || hbciTwoStepMechanism.getProcess() == 2) {
            return hktanProcess2(hbciTwoStepMechanism);
        } else {
            return hktanProcess1(hbciTwoStepMechanism);
        }
    }

    private AbstractHbciDialog createDialog(HbciDialogType dialogType, HBCICallback hbciCallback,
                                            HBCITwoStepMechanism twoStepMechanism, Map<String, String> bpd) {
        HBCICallback callback = createCallback(hbciCallback);
        HbciDialogRequest dialogRequest = createDialogRequest(callback);

        bpd = Optional.ofNullable(bpd)
            .orElseGet(() -> hbciBpdCacheHolder.getBpd(dialogRequest));

        return HbciDialogFactory.createDialog(dialogType, dialogRequest, twoStepMechanism, bpd);
    }

    void checkExecuteStatus(HBCIExecStatus execStatus) {
        if (!execStatus.isOK()) {
            throw HbciErrorUtils.toMultibankingException(execStatus.getMsgStatusList());
        }
    }

    private GVTAN2Step hktanProcess1(HBCITwoStepMechanism hbciTwoStepMechanism) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), getOrCreateHbciJob());
        hktan.setProcess(KnownTANProcess.PROCESS1);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", getOrCreateHbciJob().createOrderHash(hbciTwoStepMechanism.getSegversion()));

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(getOrCreateHbciJob(), hktan, hbciTwoStepMechanism);
        }

        hbciTanSubmit.setSepaPain(getOrCreateHbciJob().getRawData());

        dialog.addTask(hktan, false);
        return hktan;
    }

    private GVTAN2Step hktanProcess2(HBCITwoStepMechanism hbciTwoStepMechanism) {
        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport(), getOrCreateHbciJob());
        hktan.setProcess(KnownTANProcess.PROCESS2_STEP1);
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getConsent().getSelectedMethod().getMedium());
        }

        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("orderaccount", getHbciKonto());
        Optional.ofNullable(getOrCreateHbciJob())
            .map(AbstractHBCIJob::getHBCICode)
            .ifPresent(hbciCode -> hktan.setParam("ordersegcode", hbciCode));

        Optional<HBCIMessage> hbciMessage = Optional.ofNullable(getOrCreateHbciJob())
            .map(abstractHBCIJob -> dialog.addTask(abstractHBCIJob).append(hktan));
        if (!hbciMessage.isPresent()) {
            dialog.addTask(hktan);
        }

        return hktan;
    }

    Konto getHbciKonto() {
        return getPsuAccount()
            .map(account -> {
                String accountNumber = account.getAccountNumber() != null
                    ? account.getAccountNumber()
                    : Iban.valueOf(account.getIban()).getAccountNumber();

                Konto konto = dialog.getPassport().findAccountByAccountNumber(accountNumber);
                konto.iban = account.getIban();
                if (konto.bic == null) {
                    konto.bic = Optional.ofNullable(account.getBic())
                        .orElseGet(() -> HBCIUtils.getBankInfo(konto.blz).getBic());
                }
                return konto;
            })
            .orElseGet(this::getFirstAccount);
    }

    private Konto getFirstAccount() {
        //could be null in case of needed sca for loadAccounts request
        return Optional.of(dialog.getPassport().getAccounts())
            .map(kontos -> !kontos.isEmpty() ? kontos.get(0) : null)
            .orElse(null);
    }

    private Optional<BankAccount> getPsuAccount() {
        return Optional.ofNullable(transactionRequest.getTransaction().getPsuAccount());
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

    private HBCICallback createCallback(HBCICallback hbciCallback) {
        return new AbstractHBCICallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challengeInfo, String challengeHhdUc,
                                             HHDVersion.Type type) {
                //needed later for submitAuthorizationCode
                hbciTanSubmit.setOrderRef(orderRef);

                challenge = new UpdateAuthResponse(HBCI, EMBEDDED, SCAMETHODSELECTED);

                ChallengeData challengeData = new ChallengeData();
                challengeData.setAdditionalInformation(challengeInfo);
                challenge.setChallenge(challengeData);

                if (challengeHhdUc != null) {
                    MatrixCode matrixCode = MatrixCode.tryParse(challengeHhdUc);
                    if (matrixCode != null)
                        challengeData.setImage(Base64.encodeBase64String(matrixCode.getImage()));
                    else
                        challengeData.setData(Collections.singletonList(challengeHhdUc));
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
        return hbciDialogRequestMapper.toHbciDialogRequest(transactionRequest, hbciCallback);
    }

    AbstractHBCIJob getOrCreateHbciJob() {
        if (hbciJob == null) {
            hbciJob = checkVeu()
                .orElseGet(this::createHbciJob);
        }
        return hbciJob;
    }

    private Optional<AbstractHBCIJob> checkVeu() {
        if (transactionRequest.getTransaction().isVeu2ndSignature()) {
            GVVeuStep veuStep = new GVVeuStep(dialog.getPassport());
            veuStep.setParam("orderref", transactionRequest.getTransaction().getOrderId());
            veuStep.setParam("my", getHbciKonto());
            return Optional.of(veuStep);
        }
        return Optional.empty();
    }

    private HbciConsent getConsent() {
        return (HbciConsent) transactionRequest.getBankApiConsentData();
    }

    abstract String getHbciJobName();

    abstract AbstractHBCIJob createHbciJob();

    abstract R createJobResponse();

}
