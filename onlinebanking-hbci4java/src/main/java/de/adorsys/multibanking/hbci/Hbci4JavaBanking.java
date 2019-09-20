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

package de.adorsys.multibanking.hbci;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.hbci.job.*;
import de.adorsys.multibanking.hbci.model.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIMsgStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.ScaStatus.*;
import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.bpd;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.upd;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.jobs;

@Slf4j
public class Hbci4JavaBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Map<String, Map<String, String>> bpdCache;

    private HbciScaMapper hbciScaMapper = new HbciScaMapperImpl();
    private HbciObjectMapper hbciObjectMapper = new HbciObjectMapperImpl();

    public Hbci4JavaBanking() {
        this(null, false);
    }

    public Hbci4JavaBanking(boolean cacheBpdUpd) {
        this(null, cacheBpdUpd);
    }

    public Hbci4JavaBanking(InputStream customBankConfigInput, boolean cacheBpdUpd) {
        if (cacheBpdUpd) {
            bpdCache = new HashMap<>();
        }

        try (InputStream inputStream = Optional.ofNullable(customBankConfigInput)
            .orElseGet(this::getDefaultBanksInput)) {
            HBCIUtils.refreshBLZList(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new Jdk8Module());
    }

    private InputStream getDefaultBanksInput() {
        return Optional.ofNullable(HBCIUtils.class.getClassLoader().getResource("blz.properties"))
            .map(url -> {
                try {
                    return url.openStream();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            })
            .orElseThrow(() -> new RuntimeException("blz.properties not exists in classpath"));
    }

    @Override
    public BankApi bankApi() {
        return BankApi.HBCI;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(String userId) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        //not needed
    }

    private ScaMethodsResponse authenticatePsu(AuthenticatePsuRequest authenticatePsuRequest) {
        try {
            HbciDialogRequest dialogRequest = hbciObjectMapper.toHbciDialogRequest(authenticatePsuRequest, null);
            BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(dialogRequest);
            dialogRequest.setCallback(hbciCallback);

            PinTanPassport updPassport = fetchBpdUpd(dialogRequest);

            ScaMethodsResponse response = ScaMethodsResponse.builder()
                .tanTransportTypes(extractTanTransportTypes(updPassport))
                .build();
            updateUpd(hbciCallback, response);
            return response;
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                AccountInformationJob accountInformationJob = new AccountInformationJob(request);
                LoadAccountInformationResponse response = accountInformationJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            } else {
                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(new SubmitAuthorisationCode<>(request));

                removeConsentTanSubmitData(hbciConsent, submitAuthorizationCodeResponse);

                return (LoadAccountInformationResponse) submitAuthorizationCodeResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public LoadBookingsResponse loadBookings(TransactionRequest<LoadBookings> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                LoadBookingsJob loadBookingsJob = new LoadBookingsJob(request);
                LoadBookingsResponse response = loadBookingsJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            } else {
                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(new SubmitAuthorisationCode<>(request));

                removeConsentTanSubmitData(hbciConsent, submitAuthorizationCodeResponse);

                return (LoadBookingsResponse) submitAuthorizationCodeResponse.getJobResponse();
            }

        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    public LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                LoadBalancesJob loadBalancesJob = new LoadBalancesJob(request);
                LoadBalancesResponse response = loadBalancesJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            } else {
                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(new SubmitAuthorisationCode<>(request));

                removeConsentTanSubmitData(hbciConsent, submitAuthorizationCodeResponse);

                return (LoadBalancesResponse) submitAuthorizationCodeResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    private void removeConsentTanSubmitData(HbciConsent hbciConsent, SubmitAuthorizationCodeResponse<?
        extends AbstractResponse> submitAuthorizationCodeResponse) {
        hbciConsent.setHbciTanSubmit(null);
        hbciConsent.setStatus(submitAuthorizationCodeResponse.getScaStatus());
        hbciConsent.setScaAuthenticationData(null);
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    public void executeTransactionWithoutSca(TransactionRequest<AbstractScaTransaction> request) {
        checkBankExists(request.getBank());
        setRequestBpdAndCreateCallback(request);

        try {
            TransferJob transferJob = new TransferJob();
            transferJob.requestTransfer(request);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public AuthorisationCodeResponse initiatePayment(TransactionRequest request) {
        checkBankExists(request.getBank());
        BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

        try {
            ScaRequiredJob scaJob = Optional.ofNullable(request.getTransaction())
                .map(transaction -> createScaJob(request))
                .orElse(new TanRequestJob(request));

            AbstractResponse response = scaJob.authorisationAwareExecute(hbciCallback);
            updateUpd(hbciCallback, response);

            return response.getAuthorisationCodeResponse();
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCode(SubmitAuthorisationCode submitAuthorisationCode) {
        checkBankExists(submitAuthorisationCode.getOriginTransactionRequest().getBank());
        setRequestBpdAndCreateCallback(submitAuthorisationCode.getOriginTransactionRequest());
        try {
            ScaRequiredJob scaJob = createScaJob(submitAuthorisationCode.getOriginTransactionRequest());

            return new SubmitAuthorisationCodeJob<>(scaJob).sumbitAuthorizationCode(submitAuthorisationCode);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    private void checkBankExists(Bank bank) {
        String bankCode = bank.getBankApiBankCode() != null
            ? bank.getBankApiBankCode()
            : bank.getBankCode();

        Optional.ofNullable(bank.getBankingUrl()).ifPresent(s -> {
            BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
            if (bankInfo == null) {
                bankInfo = new BankInfo();
                bankInfo.setBlz(bankCode);
                bankInfo.setPinTanAddress(s);
                bankInfo.setPinTanVersion(HBCIVersion.HBCI_300);
                HBCIUtils.addBankInfo(bankInfo);
            }
        });
    }

    private BpdUpdHbciCallback setRequestBpdAndCreateCallback(AbstractRequest request) {
        String bankCode = Optional.ofNullable(request.getBank().getBankApiBankCode())
            .orElse(request.getBank().getBankCode());

        Optional.ofNullable(bpdCache).ifPresent(cache -> request.setHbciBPD(cache.get(bankCode)));
        return new BpdUpdHbciCallback(bankCode, bpdCache);
    }

    private AbstractHbciDialog createDialog(HbciDialogType dialogType, HbciDialogRequest dialogRequest,
                                            HBCITwoStepMechanism twoStepMechanism) {
        checkBankExists(dialogRequest.getBank());

        String bankCode = dialogRequest.getBank().getBankApiBankCode() != null
            ? dialogRequest.getBank().getBankApiBankCode()
            : dialogRequest.getBank().getBankCode();

        Optional.ofNullable(bpdCache)
            .ifPresent(cache -> dialogRequest.setHbciBPD(cache.get(bankCode)));
        try {
            return HbciDialogFactory.createDialog(dialogType, null, dialogRequest, twoStepMechanism);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    private PinTanPassport fetchBpdUpd(HbciDialogRequest dialogRequest) {
        AbstractHbciDialog bpdDialog = createDialog(bpd, dialogRequest, null);
        bpdDialog.execute(true);

        AbstractHbciDialog updDialog = createDialog(upd, dialogRequest, null);
        updDialog.execute(true);

        HBCIJobsDialog dialog = (HBCIJobsDialog) createDialog(jobs, dialogRequest, null);
        dialog.getPassport().setSysId(updDialog.getPassport().getSysId());

        HBCIMsgStatus hbciMsgStatus = dialog.dialogInit(false);
        if (!hbciMsgStatus.isOK()) {
            throw new MultibankingException(HBCI_ERROR, hbciMsgStatus.getErrorList()
                .stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }

        dialog.close();

        if (dialog.getPassport().jobSupported(GVTANMediaList.getLowlevelName())) {
            dialog = fetchTanMedias(dialogRequest, dialog.getPassport());
        }

        return dialog.getPassport();
    }

    private HBCIJobsDialog fetchTanMedias(HbciDialogRequest dialogRequest, PinTanPassport passport) {
        HBCITwoStepMechanism hbciTwoStepMechanism = passport.getUserTwostepMechanisms().stream()
            .filter(scaMethod -> !scaMethod.equals("999"))
            .findFirst()
            .map(scaMethod -> passport.getBankTwostepMechanisms().get(scaMethod))
            .orElseThrow(() -> new MultibankingException(MultibankingError.HBCI_ERROR, "no valid sca methods " +
                "available"));

        HBCIJobsDialog dialog = (HBCIJobsDialog) createDialog(jobs, dialogRequest, hbciTwoStepMechanism);
        dialog.dialogInit(true, "HKTAB");
        dialog.addTask(new GVTANMediaList(dialog.getPassport()));
        dialog.execute(true);
        return dialog;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        return bankInfo != null && bankInfo.getPinTanVersion() != null;
    }

    @SuppressWarnings("unchecked")
    private ScaRequiredJob createScaJob(TransactionRequest transactionRequest) {
        switch (transactionRequest.getTransaction().getTransactionType()) {
            case SINGLE_PAYMENT:
            case FUTURE_SINGLE_PAYMENT:
                return new SinglePaymentJob(transactionRequest);
            case FOREIGN_PAYMENT:
                return new ForeignPaymentJob(transactionRequest);
            case BULK_PAYMENT:
            case FUTURE_BULK_PAYMENT:
                return new BulkPaymentJob(transactionRequest);
            case STANDING_ORDER:
                return new NewStandingOrderJob(transactionRequest);
            case RAW_SEPA:
                return new RawSepaJob(transactionRequest);
            case FUTURE_SINGLE_PAYMENT_DELETE:
                return new DeleteFutureSinglePaymentJob(transactionRequest);
            case FUTURE_BULK_PAYMENT_DELETE:
                return new DeleteFutureBulkPaymentJob(transactionRequest);
            case STANDING_ORDER_DELETE:
                return new DeleteStandingOrderJob(transactionRequest);
            case TAN_REQUEST:
                return new TanRequestJob(transactionRequest);
            case LOAD_BANKACCOUNTS:
                return new AccountInformationJob(transactionRequest);
            case LOAD_BALANCES:
                return new LoadBalancesJob(transactionRequest);
            case LOAD_TRANSACTIONS:
                return new LoadBookingsJob(transactionRequest);
            default:
                throw new IllegalArgumentException("invalid transaction type " + transactionRequest.getTransaction().getTransactionType());
        }
    }

    private RuntimeException handleHbciException(HBCI_Exception e) {
        Throwable processException = e;
        while (processException.getCause() != null && !(processException.getCause() instanceof MultibankingException)) {
            processException = processException.getCause();
        }

        if (processException.getCause() instanceof MultibankingException) {
            return (MultibankingException) processException.getCause();
        }

        return e;
    }

    private void updateUpd(BpdUpdHbciCallback bpdUpdHbciCallback, AbstractResponse response) {
        Optional.ofNullable(bpdUpdHbciCallback)
            .ifPresent(callback -> {
                response.setHbciUpd(callback.getUpd());
                response.setHbciSysId(callback.getSysId());
            });
    }

    private List<TanTransportType> extractTanTransportTypes(PinTanPassport hbciPassport) {
        return hbciPassport.getUserTwostepMechanisms()
            .stream()
            .map(id -> hbciPassport.getBankTwostepMechanisms().get(id))
            .filter(Objects::nonNull)
            .map(hbciTwoStepMechanism -> TanTransportType.builder()
                .id(hbciTwoStepMechanism.getSecfunc())
                .name(hbciTwoStepMechanism.getName())
                .inputInfo(hbciTwoStepMechanism.getInputinfo())
                .needTanMedia(hbciTwoStepMechanism.getNeedtanmedia().equals("2"))
                .build())
            .map(tanTransportType -> {
                if (!tanTransportType.isNeedTanMedia()) {
                    return Collections.singletonList(tanTransportType);
                } else {
                    return hbciPassport.getTanMedias().stream()
                        .map(tanMediaInfo -> {
                            TanTransportType clone = SerializationUtils.clone(tanTransportType);
                            clone.setMedium(tanMediaInfo.mediaName);
                            return clone;
                        })
                        .collect(Collectors.toList());
                }
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable() {

            @Override
            public CreateConsentResponse createConsent(Consent consent, boolean redirectPreferred,
                                                       String tppRedirectUri) {
                HbciConsent hbciConsent = new HbciConsent();
                hbciConsent.setStatus(STARTED);

                return hbciScaMapper.toCreateConsentResponse(hbciConsent);
            }

            @Override
            public Consent getConsent(String consentId) {
                return null;
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                HbciConsent hbciConsent = (HbciConsent) updatePsuAuthentication.getBankApiConsentData();
                hbciConsent.setCredentials(updatePsuAuthentication.getCredentials());

                AuthenticatePsuRequest request = hbciScaMapper.toAuthenticatePsuRequest(updatePsuAuthentication);
                ScaMethodsResponse response = authenticatePsu(request);

                hbciConsent.setTanMethodList(response.getTanTransportTypes());
                hbciConsent.setStatus(PSUAUTHENTICATED);

                return hbciScaMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                HbciConsent hbciConsent = (HbciConsent) transactionAuthorisation.getBankApiConsentData();
                hbciConsent.setScaAuthenticationData(transactionAuthorisation.getScaAuthenticationData());

                return hbciScaMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                HbciConsent hbciConsent = (HbciConsent) selectPsuAuthenticationMethod.getBankApiConsentData();

                TanTransportType selectedMethod = hbciConsent.getTanMethodList().stream()
                    .filter(tanTransportType -> tanTransportType.getId().equals(selectPsuAuthenticationMethod.getAuthenticationMethodId()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
                hbciConsent.setSelectedMethod(selectedMethod);
                hbciConsent.setStatus(SCAMETHODSELECTED);

                return hbciScaMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public void revokeConsent(String consentId) {
                //noop
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                             Object bankApiConsentData) {
                HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;
                return hbciScaMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                        Object bankApiConsentData) {
                HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;

                if (hbciConsent.getHbciTanSubmit() != null && hbciConsent.getScaAuthenticationData() == null) {
                    throw new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS);
                }

                if (hbciConsent.getStatus() == SCAMETHODSELECTED || hbciConsent.getStatus() == FINALISED) {
                    return;
                }
                throw new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS);
            }

            @Override
            public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
                //tansubmit persistence fur further call
                HbciConsent hbciConsent = (HbciConsent) bankApiConsentData;
                hbciConsent.setHbciTanSubmit(authorisationCodeResponse.getTanSubmit());
            }
        };
    }

    @RequiredArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class BpdUpdHbciCallback extends AbstractHBCICallback {

        private final String bankCode;
        private final Map<String, Map<String, String>> bpdCache;
        private Map<String, String> upd;
        private String sysId;

        @SuppressWarnings("unchecked")
        @Override
        public void status(int statusTag, Object o) {
            if (statusTag == STATUS_INST_BPD_INIT_DONE) {
                Optional.of(bpdCache).ifPresent(cache -> cache.put(bankCode, (Map<String, String>) o));
            } else if (statusTag == STATUS_INIT_UPD_DONE) {
                this.upd = (Map<String, String>) o;
            }
        }

        @Override
        public void callback(int reason, List<String> messages, int datatype, StringBuilder retData) {
            if (reason == WRONG_PIN) {
                throw new MultibankingException(INVALID_PIN, messages.stream()
                    .map(messageString -> Message.builder().renderedMessage(messageString).build())
                    .collect(Collectors.toList()));
            }

        }

        @Override
        public void status(int statusTag, Object[] o) {
            if (statusTag == STATUS_INIT_SYSID_DONE) {
                this.sysId = o[1].toString();
            }
        }
    }
}
