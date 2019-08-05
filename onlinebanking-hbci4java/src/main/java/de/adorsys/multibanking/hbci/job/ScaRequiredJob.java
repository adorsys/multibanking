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
import de.adorsys.multibanking.domain.Product;
import de.adorsys.multibanking.domain.TanChallenge;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.hbci.model.*;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.ChallengeInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.manager.HHDVersion;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_SCA_METHOD;
import static de.adorsys.multibanking.hbci.job.AccountInformationJob.extractTanTransportTypes;
import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.startHbciDialog;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

public abstract class ScaRequiredJob {

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public AuthorisationCodeResponse execute(HbciCallback bpdCallback) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        AuthorisationCodeResponse response = AuthorisationCodeResponse.builder().tanSubmit(hbciTanSubmit).build();

        Optional.ofNullable(getTransactionRequest().getTransaction())
            .ifPresent(transaction -> hbciTanSubmit.setOriginJobName(getHbciJobName(transaction.getTransactionType())));

        HBCIDialog dialog = startHbciDialog(null, createDialogRequest(createCallback(bpdCallback, response)));

        AbstractHBCIJob hbciJob = createHbciJob(dialog.getPassport());
        beforeExecute(dialog);

        //hbciJob could be null in case of tan request without corresponding hbci request
        if (hbciJob == null || dialog.getPassport().tan2StepRequired(hbciJob.getHBCICode())) {
            HBCITwoStepMechanism userTanTransportType = getUserTanTransportType(dialog);

            if (hbciJob == null && userTanTransportType == null) {
                throw new MultibankingException(INVALID_SCA_METHOD,
                    "inavalid two stem mechanism: " + getTransactionRequest().getTanTransportType().getId());
            }

            if (userTanTransportType == null && dialog.getPassport().tan2StepRequired(hbciJob.getHBCICode())) {
                response.setTanTransportTypes(extractTanTransportTypes(dialog.getPassport()));
                return response;
            }
            handleSCA(hbciTanSubmit, dialog, hbciJob, userTanTransportType);
        } else {
            //No SCA needed
            dialog.addTask(hbciJob);
        }

        HBCIExecStatus status = dialog.execute(false);
        if (!status.isOK()) {
            throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages());
        }

        afterExecute(dialog);

        if (hbciJob == null || dialog.getPassport().tan2StepRequired(hbciJob.getHBCICode())) {
            updateTanSubmit(hbciTanSubmit, dialog, hbciJob);
        }

        return response;
    }

    private void handleSCA(HbciTanSubmit hbciTanSubmit, HBCIDialog dialog,
                           AbstractHBCIJob hbciJob, HBCITwoStepMechanism hbciTwoStepMechanism) {
        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (hbciTwoStepMechanism.getProcess() == 1) {
            hbciTanSubmit.setSepaPain(hktanProcess1(hbciTwoStepMechanism, hbciJob, hktan));
            dialog.addTask(hktan, false);
        } else {
            hktanProcess2(dialog, hbciJob, getDebtorAccount(dialog.getPassport()), hktan);
        }

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", getTransactionRequest().getTanTransportType().getMedium());
        }
    }

    private void updateTanSubmit(HbciTanSubmit hbciTanSubmit, HBCIDialog dialog, AbstractHBCIJob hbciJob) {
        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setTwoStepMechanism(getUserTanTransportType(dialog));
        Optional.ofNullable(hbciJob)
            .ifPresent(abstractSEPAGV -> {
                Optional.ofNullable(abstractSEPAGV.getPainVersion())
                    .ifPresent(painVersion -> hbciTanSubmit.setPainVersion(painVersion.getURN()));
                hbciTanSubmit.setOriginLowLevelName(abstractSEPAGV.getJobName());
                hbciTanSubmit.setOriginSegVersion(abstractSEPAGV.getSegVersion());
                hbciTanSubmit.setHbciJobName(abstractSEPAGV.getHBCICode());
            });
    }

    private HbciCallback createCallback(HbciCallback bpdCallback, AuthorisationCodeResponse response) {
        return new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge, String challengeHhdUc,
                                             HHDVersion.Type type) {
                //needed later for submitAuthorizationCode
                ((HbciTanSubmit) response.getTanSubmit()).setOrderRef(orderRef);
                if (challenge != null) {
                    response.setChallenge(TanChallenge.builder()
                        .title(challenge)
                        .data(challengeHhdUc)
                        .build());
                }
            }

            @Override
            public void status(int statusTag, Object o) {
                if (statusTag == HBCICallback.STATUS_INST_BPD_INIT_DONE) {
                    Optional.ofNullable(bpdCallback)
                        .ifPresent(hbciCallback1 -> hbciCallback1.status(statusTag, o));
                }
            }
        };
    }

    private String hktanProcess1(HBCITwoStepMechanism hbciTwoStepMechanism, AbstractHBCIJob hbciJob, GVTAN2Step hktan) {
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

        return hbciJob.getRawData();
    }

    private void hktanProcess2(HBCIDialog dialog, AbstractHBCIJob hbciJob, Konto orderAccount, GVTAN2Step hktan) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("orderaccount", orderAccount);

        Optional<List<AbstractHBCIJob>> messages = Optional.ofNullable(hbciJob)
            .map(dialog::addTask);

        if (messages.isPresent()) {
            messages.get().add(hktan);
        } else {
            dialog.addTask(hktan);
        }
    }

    public SubmitAuthorizationCodeResponse sumbitAuthorizationCode(SubmitAuthorizationCodeRequest submitAuthorizationCodeRequest) {
        HbciTanSubmit hbciTanSubmit = evaluateTanSubmit(submitAuthorizationCodeRequest);

        HbciPassport hbciPassport = createPassport(submitAuthorizationCodeRequest, hbciTanSubmit);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());
        AbstractHBCIJob paymentGV;

        if (hbciTanSubmit.getTwoStepMechanism().getProcess() == 1) {
            paymentGV = submitProcess1(submitAuthorizationCodeRequest.getSepaTransaction(), hbciTanSubmit, hbciPassport,
                hbciDialog);
        } else {
            paymentGV = submitProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages());
        } else {
            return createResponse(hbciTanSubmit, paymentGV, status);
        }
    }

    private AbstractHBCIJob submitProcess1(AbstractScaTransaction transaction, HbciTanSubmit hbciTanSubmit, HbciPassport
        hbciPassport, HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        AbstractHBCIJob uebSEPAJob = createHbciJob(hbciPassport);
        if (hbciTanSubmit.getSepaPain() != null) {
            uebSEPAJob.getConstraints().remove("_sepapain"); //prevent pain generation
            uebSEPAJob.setLowlevelParam(uebSEPAJob.getName() + ".sepapain", hbciTanSubmit.getSepaPain());
        }
        hbciDialog.addTask(uebSEPAJob);
        return uebSEPAJob;
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

        GVTAN2Step hktan = new GVTAN2Step(hbciDialog.getPassport());
        hktan.setOriginJob(originJob);
        hktan.setParam("orderref", hbciTanSubmit.getOrderRef());
        hktan.setParam("process", hbciTanSubmit.getHktanProcess() != null ? hbciTanSubmit.getHktanProcess() : "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
        return originJob;
    }

    private HbciTanSubmit deserializeTanSubmit(byte[] data) {
        try {
            return objectMapper().readValue(data, HbciTanSubmit.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize HbciTanSubmit", e);
        }
    }

    Konto getDebtorAccount(PinTanPassport passport) {
        return Optional.ofNullable(getTransactionRequest().getTransaction().getDebtorBankAccount())
            .map(bankAccount -> {
                Konto konto = passport.findAccountByAccountNumber(bankAccount.getAccountNumber());
                konto.iban = bankAccount.getIban();
                konto.bic = bankAccount.getBic();
                return konto;
            })
            .orElse(null);
    }

    private HbciPassport createPassport(SubmitAuthorizationCodeRequest submitAuthorizationCodeRequest,
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
                    return submitAuthorizationCodeRequest.getTan();
                }
            });
        state.apply(hbciPassport);

        hbciPassport.setPIN(submitAuthorizationCodeRequest.getPin());
        hbciPassport.setCurrentSecMechInfo(hbciTanSubmit.getTwoStepMechanism());
        hbciPassport.setBPD(bpd);

        return hbciPassport;
    }

    private HbciTanSubmit evaluateTanSubmit(SubmitAuthorizationCodeRequest submitAuthorizationCodeRequest) {
        if (submitAuthorizationCodeRequest.getTanSubmit() instanceof HbciTanSubmit) {
            return (HbciTanSubmit) submitAuthorizationCodeRequest.getTanSubmit();
        } else {
            return deserializeTanSubmit((byte[]) submitAuthorizationCodeRequest.getTanSubmit());
        }
    }

    private SubmitAuthorizationCodeResponse createResponse(HbciTanSubmit hbciTanSubmit, AbstractHBCIJob paymentGV,
                                                           HBCIExecStatus status) {
        String transactionId = Optional.ofNullable(paymentGV)
            .map(abstractHBCIJob -> orderIdFromJobResult(abstractHBCIJob.getJobResult()))
            .orElse(hbciTanSubmit.getOrderRef());

        SubmitAuthorizationCodeResponse response = SubmitAuthorizationCodeResponse.builder()
            .transactionId(transactionId)
            .build();

        if (!status.getDialogStatus().msgStatusList.isEmpty()) {
            response.setStatus(status.getDialogStatus().msgStatusList.get(0).segStatus.toString());
        }
        return response;
    }

    HBCITwoStepMechanism getUserTanTransportType(HBCIDialog dialog) {
        return Optional.ofNullable(getTransactionRequest().getTanTransportType())
            .map(tanTransportType -> dialog.getPassport().getBankTwostepMechanisms().get(tanTransportType.getId()))
            .orElse(null);
    }

    private HbciDialogRequest createDialogRequest(HbciCallback hbciCallback) {
        TransactionRequest transactionRequest = getTransactionRequest();

        return HbciDialogRequest.builder()
            .bankCode(transactionRequest.getBankCode() != null ? transactionRequest.getBankCode() :
                transactionRequest.getBankAccess().getBankCode())
            .customerId(transactionRequest.getBankAccess().getBankLogin())
            .login(transactionRequest.getBankAccess().getBankLogin2())
            .hbciPassportState(transactionRequest.getBankAccess().getHbciPassportState())
            .pin(transactionRequest.getPin())
            .callback(hbciCallback)
            .product(Optional.ofNullable(transactionRequest.getProduct())
                .map(product -> new Product(product.getName(), product.getVersion()))
                .orElse(null))
            .bpd(transactionRequest.getBpd())
            .build();
    }

    abstract TransactionRequest getTransactionRequest();

    abstract String getHbciJobName(AbstractScaTransaction.TransactionType transactionType);

    abstract String orderIdFromJobResult(HBCIJobResult jobResult);

    abstract AbstractHBCIJob createHbciJob(PinTanPassport passport);

    abstract void beforeExecute(HBCIDialog dialog);

    abstract void afterExecute(HBCIDialog dialog);

}
