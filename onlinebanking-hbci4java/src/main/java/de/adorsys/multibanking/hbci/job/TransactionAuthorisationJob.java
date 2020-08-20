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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.TransactionAuthorisationResponse;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.TransactionAuthorisation;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import de.adorsys.multibanking.hbci.model.HbciDialogFactory;
import de.adorsys.multibanking.hbci.model.HbciPassport;
import de.adorsys.multibanking.hbci.model.HbciTanSubmit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.manager.KnownTANProcess;
import org.kapott.hbci.status.HBCIExecStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static de.adorsys.multibanking.domain.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.hbci.HbciCacheHandler.getBpd;

@Slf4j
public class TransactionAuthorisationJob<T extends AbstractTransaction, R extends AbstractResponse> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .findAndRegisterModules();

    private final ScaAwareJob<T, R> scaJob;
    private final TransactionAuthorisation<T> transactionAuthorisation;
    private final HbciConsent consent;

    private final HBCIJobsDialog hbciDialog;

    public TransactionAuthorisationJob(ScaAwareJob<T, R> scaJob, TransactionAuthorisation<T> transactionAuthorisation) {
        this.scaJob = scaJob;
        this.transactionAuthorisation = transactionAuthorisation;

        consent = (HbciConsent) transactionAuthorisation.getOriginTransactionRequest().getBankApiConsentData();
        scaJob.hbciTanSubmit = evaluateTanSubmit();

        hbciDialog = new HBCIJobsDialog(createPassport(), scaJob.hbciTanSubmit.getDialogId(),
            scaJob.hbciTanSubmit.getMsgNum());
        scaJob.dialog = hbciDialog;
    }

    public TransactionAuthorisationResponse<R> execute() {
        if (scaJob.hbciTanSubmit.getTwoStepMechanism().getProcess() == 1)
            submitProcess1();
        else
            submitProcess2();

        HBCIExecStatus hbciExecStatus = hbciDialog.execute(false);
        if (!hbciExecStatus.isOK()) {
            if (consent.isCloseDialog()) {
                hbciDialog.dialogEnd();
            }
            throw new MultibankingException(HBCI_ERROR, scaJob.msgStatusListToPsuMessages(hbciExecStatus.getMsgStatusList()));
        }

        if (StringUtils.equals("HKIDN", scaJob.hbciTanSubmit.getHbciJobName())) {
            if (!hbciExecStatus.getMsgStatusList().isEmpty()) {
                hbciDialog.getPassport().updateUPD(hbciExecStatus.getMsgStatusList().get(0).getData());
            }

            //sca for dialoginit was needed -> fints consent active, expecting response with exempted sca
            TransactionAuthorisationResponse<R> response = new TransactionAuthorisationResponse<>(scaJob.execute(null));
            response.setScaStatus(FINALISED);
            return response;
        } else if (consent.isCloseDialog()) {
            hbciDialog.dialogEnd();
        }
        scaJob.hbciTanSubmit.setMsgNum(scaJob.hbciTanSubmit.getMsgNum() + 1);
        return createResponse(hbciExecStatus);
    }

    private void submitProcess1() {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        AbstractHBCIJob hbciJob = scaJob.getHbciJob();

        if (scaJob.hbciTanSubmit.getSepaPain() != null) {
            hbciJob.getConstraints().remove("_sepapain"); //prevent pain generation
            hbciJob.setLowlevelParam(hbciJob.getName() + ".sepapain", scaJob.hbciTanSubmit.getSepaPain());
        }
        hbciDialog.addTask(hbciJob);
    }

    @SuppressWarnings("unchecked")
    private void submitProcess2() {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        AbstractHBCIJob originJob = Optional.ofNullable(scaJob.hbciTanSubmit.getOriginJobName())
            .map(originJobName -> {
                AbstractHBCIJob hbciJob = scaJob.getHbciJob();
                try {
                    hbciJob.setLlParams(objectMapper.readValue(scaJob.hbciTanSubmit.getLowLevelParams(), HashMap.class));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new MultibankingException(INTERNAL_ERROR, 500, e.getMessage());
                }
                hbciJob.setSegVersion(scaJob.hbciTanSubmit.getOriginSegVersion());
                return hbciJob;
            }).orElse(null);

        GVTAN2Step hktan = new GVTAN2Step(hbciDialog.getPassport(), originJob);
        hktan.setProcess(KnownTANProcess.PROCESS2_STEP2);
        Optional.ofNullable(scaJob.hbciTanSubmit.getHbciJobName())
            .ifPresent(hbciSegCode -> hktan.setParam("ordersegcode", hbciSegCode));
        hktan.setVeu(scaJob.hbciTanSubmit.isVeu());
        hktan.setParam("orderref", scaJob.hbciTanSubmit.getOrderRef());
        hktan.setParam("process", scaJob.hbciTanSubmit.getHktanProcess() != null ? scaJob.hbciTanSubmit.getHktanProcess() : "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
    }

    private TransactionAuthorisationResponse<R> createResponse(HBCIExecStatus hbciExecStatus) {
        R jobResponse = scaJob.createJobResponse();
        jobResponse.setMessages(scaJob.msgStatusListToPsuMessages(hbciExecStatus.getMsgStatusList()));

        TransactionAuthorisationResponse<R> response = new TransactionAuthorisationResponse<>(jobResponse);

        //HKIDN -> FINALISED -> further request like HKCAZ already executed
        ScaStatus scaStatus = Optional.ofNullable(scaJob.hbciTanSubmit.getHbciJobName())
            .map(hbciJobName -> hbciJobName.equals("HKIDN") || scaJob.transactionRequest.getTransaction() instanceof AbstractPayment)
            .map(finalised -> Boolean.TRUE.equals(finalised) ? FINALISED : SCAMETHODSELECTED)
            .orElse(FINALISED);

        response.setScaStatus(scaStatus);
        return response;
    }

    private HbciTanSubmit evaluateTanSubmit() {
        if (consent.getHbciTanSubmit() instanceof HbciTanSubmit) {
            return (HbciTanSubmit) consent.getHbciTanSubmit();
        } else {
            return deserializeTanSubmit((byte[]) consent.getHbciTanSubmit());
        }
    }

    private HbciTanSubmit deserializeTanSubmit(byte[] data) {
        try {
            return objectMapper.readValue(data, HbciTanSubmit.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize HbciTanSubmit", e);
        }
    }

    private HbciPassport createPassport() {
        Map<String, String> bpd =
            Optional.ofNullable(getBpd(transactionAuthorisation.getOriginTransactionRequest()))
                .orElseGet(() -> scaJob.fetchBpd(null).getBPD());

        HbciPassport.State state = HbciPassport.State.fromJson(scaJob.hbciTanSubmit.getPassportState());

        HbciPassport passport = HbciDialogFactory.createPassport(state,
            new AbstractHBCICallback() {
                @Override
                public String needTAN() {
                    return ((HbciConsent) transactionAuthorisation.getOriginTransactionRequest().getBankApiConsentData()).getScaAuthenticationData();
                }
            });
        state.apply(passport);

        HbciConsent hbciConsent =
            (HbciConsent) transactionAuthorisation.getOriginTransactionRequest().getBankApiConsentData();

        passport.setPIN(hbciConsent.getCredentials().getPin());
        passport.setCurrentSecMechInfo(scaJob.hbciTanSubmit.getTwoStepMechanism());
        passport.setBPD(bpd);

        return passport;
    }

}
