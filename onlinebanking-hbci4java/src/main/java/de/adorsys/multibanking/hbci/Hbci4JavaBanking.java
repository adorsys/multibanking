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
import org.apache.commons.lang3.SerializationUtils;
import org.iban4j.Iban;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.GV_Result.GVRTANMediaList;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.dialog.HBCIUpdDialog;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.ScaStatus.*;
import static de.adorsys.multibanking.domain.exception.MultibankingError.BANK_NOT_SUPPORTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.*;

public class Hbci4JavaBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Map<String, Map<String, String>> bpdCache;

    private HbciScaMapper hbciScaMapper = new HbciScaMapperImpl();
    private HbciDialogRequestMapper hbciDialogRequestMapper = new HbciDialogRequestMapperImpl();

    public Hbci4JavaBanking() {
        this(null, false);
    }

    public Hbci4JavaBanking(boolean cacheBpdUpd) {
        this(null, cacheBpdUpd);
    }

    public Hbci4JavaBanking(InputStream customBankConfigInput, boolean cacheBpdUpd) {
        if (cacheBpdUpd) {
            bpdCache = new ConcurrentHashMap<>();
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

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    private ScaMethodsResponse authenticatePsu(AuthenticatePsuRequest authenticatePsuRequest) {
        try {
            HbciDialogRequest dialogRequest = hbciDialogRequestMapper.toHbciDialogRequest(authenticatePsuRequest, null);
            BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(dialogRequest);
            dialogRequest.setCallback(hbciCallback);

            HBCIExecStatus bpdExecStatus = fetchBpd(dialogRequest);
            boolean withHktan = !bpdExecStatus.hasMessage("9400");
            if (!withHktan) {
                HbciConsent hbciConsent = (HbciConsent) authenticatePsuRequest.getBankApiConsentData();
                hbciConsent.setWithHktan(false);
            }

            PinTanPassport passport = fetchUpd(dialogRequest, withHktan);

            Map<String, List<GVRTANMediaList.TANMediaInfo>> tanMediaMap = null;
            if (passport.jobSupported(GVTANMediaList.getLowlevelName())) {
                tanMediaMap = fetchTanMedias(dialogRequest, passport);
            }

            ScaMethodsResponse response = ScaMethodsResponse.builder()
                .tanTransportTypes(extractTanTransportTypes(passport, tanMediaMap))
                .build();
            updateUpd(hbciCallback, response);
            return response;
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                AccountInformationJob accountInformationJob = new AccountInformationJob(request);
                AccountInformationResponse response = accountInformationJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                afterTransactionAuthorisation(hbciConsent, submitAuthorizationCodeResponse.getScaStatus());

                return (AccountInformationResponse) submitAuthorizationCodeResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                LoadTransactionsJob loadBookingsJob = new LoadTransactionsJob(request);
                TransactionsResponse response = loadBookingsJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                afterTransactionAuthorisation(hbciConsent, submitAuthorizationCodeResponse.getScaStatus());

                return (TransactionsResponse) submitAuthorizationCodeResponse.getJobResponse();
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
                TransactionAuthorisationResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                afterTransactionAuthorisation(hbciConsent, submitAuthorizationCodeResponse.getScaStatus());

                return (LoadBalancesResponse) submitAuthorizationCodeResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public AbstractResponse executePayment(TransactionRequest<AbstractPayment> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request);

                ScaRequiredJob scaJob = Optional.ofNullable(request.getTransaction())
                    .map(transaction -> createScaJob(request))
                    .orElse(new TanRequestJob(request));

                AbstractResponse response = scaJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);

                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                afterTransactionAuthorisation(hbciConsent, submitAuthorizationCodeResponse.getScaStatus());

                return submitAuthorizationCodeResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    private void afterTransactionAuthorisation(HbciConsent hbciConsent, ScaStatus scaStatus) {
        hbciConsent.setHbciTanSubmit(null);
        hbciConsent.setStatus(scaStatus);
        hbciConsent.setScaAuthenticationData(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisation(TransactionAuthorisation submitAuthorisationCode) {
        checkBankExists(submitAuthorisationCode.getOriginTransactionRequest().getBank());
        setRequestBpdAndCreateCallback(submitAuthorisationCode.getOriginTransactionRequest());
        try {
            ScaRequiredJob scaJob = createScaJob(submitAuthorisationCode.getOriginTransactionRequest());

            TransactionAuthorisationResponse submitAuthorizationCodeResponse =
                new SubmitAuthorisationCodeJob<>(scaJob).sumbitAuthorizationCode(submitAuthorisationCode);

            afterTransactionAuthorisation((HbciConsent) submitAuthorisationCode.getOriginTransactionRequest().getBankApiConsentData(), submitAuthorizationCodeResponse.getScaStatus());

            return submitAuthorizationCodeResponse;
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

    public AbstractHbciDialog createDialog(HbciDialogType dialogType, HbciDialogRequest dialogRequest,
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

    private HBCIExecStatus fetchBpd(HbciDialogRequest dialogRequest) {
        AbstractHbciDialog dialog = createDialog(BPD, dialogRequest, null);
        return dialog.execute(true);
    }

    private PinTanPassport fetchUpd(HbciDialogRequest dialogRequest, boolean withHktan) {
        HBCIUpdDialog dialog = (HBCIUpdDialog) createDialog(UPD, dialogRequest, null);
        dialog.setWithHktan(withHktan);
        dialog.execute(true);

        return dialog.getPassport();
    }

    private Map<String, List<GVRTANMediaList.TANMediaInfo>> fetchTanMedias(HbciDialogRequest dialogRequest,
                                                                           PinTanPassport passport) {
        List<HBCITwoStepMechanism> tanMediaNeededScaMethods = passport.getUserTwostepMechanisms().stream()
            .filter(scaMethodId -> !scaMethodId.equals("999"))
            .filter(scaMethodId -> {
                HBCITwoStepMechanism hbciTwoStepMechanismBpd = passport.getBankTwostepMechanisms().get(scaMethodId);
                return hbciTwoStepMechanismBpd != null && hbciTwoStepMechanismBpd.getNeedtanmedia().equals("2");
            })
            .map(scaMethod -> passport.getBankTwostepMechanisms().get(scaMethod))
            .collect(Collectors.toList());

        Map<String, List<GVRTANMediaList.TANMediaInfo>> tanMediaMap = new HashMap<>();
        tanMediaNeededScaMethods.forEach(twoStepMechanism -> {
            GVTANMediaList gvtanMediaList = new GVTANMediaList(passport);

            HBCIJobsDialog dialog = (HBCIJobsDialog) createDialog(JOBS, dialogRequest, twoStepMechanism);
            dialog.dialogInit(true, "HKTAB");
            dialog.addTask(gvtanMediaList);
            dialog.execute(true);

            tanMediaMap.put(twoStepMechanism.getSecfunc(),
                ((GVRTANMediaList) gvtanMediaList.getJobResult()).mediaList());
        });

        return tanMediaMap;
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
                return new LoadTransactionsJob(transactionRequest);
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

    private List<TanTransportType> extractTanTransportTypes(PinTanPassport hbciPassport, Map<String,
        List<GVRTANMediaList.TANMediaInfo>> tanMediaMap) {
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
                    return tanMediaMap.get(tanTransportType.getId()).stream()
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
                String bankCode = Iban.valueOf(consent.getPsuAccountIban()).getBankCode();
                if (!bankSupported(bankCode)) {
                    throw new MultibankingException(BANK_NOT_SUPPORTED);
                }

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

            @Override
            public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @RequiredArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class BpdUpdHbciCallback extends AbstractHBCICallback {

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
