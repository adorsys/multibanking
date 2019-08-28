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
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;
import de.adorsys.multibanking.domain.transaction.SubmitAuthorisationCode;
import de.adorsys.multibanking.hbci.model.*;
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@RequiredArgsConstructor
public class SubmitAuthorisationCodeJob<J extends ScaRequiredJob> {

    private final J scaJob;

    public SubmitAuthorizationCodeResponse sumbitAuthorizationCode(TransactionRequest<SubmitAuthorisationCode> submitAuthorizationCodeRequest) {
        HbciTanSubmit hbciTanSubmit =
            evaluateTanSubmit((HBCIConsent) submitAuthorizationCodeRequest.getBankApiConsentData());

        HbciPassport hbciPassport = createPassport(submitAuthorizationCodeRequest, hbciTanSubmit);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());
        AbstractHBCIJob hbciJob;

        if (hbciTanSubmit.getTwoStepMechanism().getProcess() == 1) {
            hbciJob = submitProcess1(hbciTanSubmit, hbciPassport, hbciDialog);
        } else {
            hbciJob = submitProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages().stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        } else {
            return createResponse(hbciPassport, hbciTanSubmit, hbciJob, status);
        }
    }

    private AbstractHBCIJob submitProcess1(HbciTanSubmit hbciTanSubmit, HbciPassport hbciPassport,
                                           HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        AbstractHBCIJob hbciJob = scaJob.createScaMessage(hbciPassport);

        if (hbciTanSubmit.getSepaPain() != null) {
            hbciJob.getConstraints().remove("_sepapain"); //prevent pain generation
            hbciJob.setLowlevelParam(hbciJob.getName() + ".sepapain", hbciTanSubmit.getSepaPain());
        }
        hbciDialog.addTask(hbciJob);
        return hbciJob;
    }

    private AbstractHBCIJob submitProcess2(HbciTanSubmit hbciTanSubmit, HBCIDialog hbciDialog) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        AbstractHBCIJob originJob = Optional.ofNullable(hbciTanSubmit.getOriginJobName())
            .map(s -> {
                AbstractHBCIJob result = newJob(hbciTanSubmit.getOriginJobName(), hbciDialog.getPassport());
                result.setSegVersion(hbciTanSubmit.getOriginSegVersion());
                return result;
            }).orElse(null);

        GVTAN2Step hktan = new GVTAN2Step(hbciDialog.getPassport(), originJob, true);
        hktan.setParam("orderref", hbciTanSubmit.getOrderRef());
        hktan.setParam("process", hbciTanSubmit.getHktanProcess() != null ? hbciTanSubmit.getHktanProcess() : "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
        return originJob;
    }

    private SubmitAuthorizationCodeResponse<?> createResponse(PinTanPassport passport, HbciTanSubmit hbciTanSubmit,
                                                              AbstractHBCIJob hbciJob, HBCIExecStatus status) {
        String transactionId = Optional.ofNullable(hbciJob)
            .map(abstractHBCIJob -> orderIdFromJobResult(abstractHBCIJob.getJobResult()))
            .orElse(hbciTanSubmit.getOrderRef());

        SubmitAuthorizationCodeResponse<?> response =
            new SubmitAuthorizationCodeResponse<>(scaJob.createJobResponse(passport, hbciJob));
        response.setTransactionId(transactionId);


        if (!status.getDialogStatus().msgStatusList.isEmpty()) {
            response.setStatus(status.getDialogStatus().msgStatusList.get(0).segStatus.toString());
        }

        return response;
    }

    private HbciTanSubmit evaluateTanSubmit(HBCIConsent hbciConsent) {
        if (hbciConsent.getHbciTanSubmit() instanceof HbciTanSubmit) {
            return (HbciTanSubmit) hbciConsent.getHbciTanSubmit();
        } else {
            return deserializeTanSubmit((byte[]) hbciConsent.getHbciTanSubmit());
        }
    }

    private HbciTanSubmit deserializeTanSubmit(byte[] data) {
        try {
            return objectMapper().readValue(data, HbciTanSubmit.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize HbciTanSubmit", e);
        }
    }

    private HbciPassport createPassport(TransactionRequest<SubmitAuthorisationCode> submitAuthorizationCodeRequest,
                                        HbciTanSubmit hbciTanSubmit) {
        Map<String, String> bpd = new HashMap<>();
        bpd.put("Params." + hbciTanSubmit.getOriginLowLevelName() + "Par" + hbciTanSubmit.getOriginSegVersion() +
            ".Par" + hbciTanSubmit.getOriginLowLevelName() + ".suppformats", hbciTanSubmit.getPainVersion());
        bpd.put("Params." + hbciTanSubmit.getOriginLowLevelName() + "Par" + hbciTanSubmit.getOriginSegVersion() +
            ".SegHead.code", hbciTanSubmit.getHbciJobName());
        bpd.put("Params.TAN2StepPar" + hbciTanSubmit.getTwoStepMechanism().getSegversion() + ".SegHead.code", "HKTAN");
        bpd.put("BPA.numgva", "100"); //dummy value

        HbciPassport.State state = HbciPassport.State.fromJson(hbciTanSubmit.getPassportState());

        HbciPassport hbciPassport = HbciDialogFactory.createPassport(state,
            new HbciCallback() {

                @Override
                public String needTAN() {
                    return ((HBCIConsent) submitAuthorizationCodeRequest.getBankApiConsentData()).getScaAuthenticationData();
                }
            });
        state.apply(hbciPassport);

        HBCIConsent hbciConsent = (HBCIConsent) submitAuthorizationCodeRequest.getBankApiConsentData();

        hbciPassport.setPIN(hbciConsent.getCredentials().getPin());
        hbciPassport.setCurrentSecMechInfo(hbciTanSubmit.getTwoStepMechanism());
        hbciPassport.setBPD(bpd);

        return hbciPassport;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    private String orderIdFromJobResult(HBCIJobResult jobResult) {
        return scaJob.orderIdFromJobResult(jobResult);
    }
}
