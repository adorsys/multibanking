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
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.hbci.job.*;
import de.adorsys.multibanking.hbci.model.HBCIConsent;
import de.adorsys.multibanking.hbci.model.HbciCallback;
import de.adorsys.multibanking.hbci.model.HbciDialogFactory;
import de.adorsys.multibanking.hbci.model.HbciDialogRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static de.adorsys.multibanking.domain.ScaStatus.STARTED;
import static de.adorsys.multibanking.hbci.job.AccountInformationJob.extractTanTransportTypes;

@Slf4j
public class Hbci4JavaBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Map<String, Map<String, String>> bpdCache;

    private HbciScaMapper hbciMapper = new HbciScaMapperImpl();

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
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
            .credentials(authenticatePsuRequest.getCredentials())
            .build();

        dialogRequest.setBank(authenticatePsuRequest.getBank());
        dialogRequest.setHbciProduct(Optional.ofNullable(authenticatePsuRequest.getHbciProduct())
            .map(product -> new Product(product.getName(), product.getVersion()))
            .orElse(null));
        dialogRequest.setHbciBPD(authenticatePsuRequest.getHbciBPD());
        dialogRequest.setHbciUPD(authenticatePsuRequest.getHbciUPD());
        dialogRequest.setHbciSysId(authenticatePsuRequest.getHbciSysId());

        BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(dialogRequest.getBank(), dialogRequest);
        dialogRequest.setCallback(hbciCallback);

        HBCIDialog dialog = createDialog(authenticatePsuRequest.getBank(), dialogRequest);

        if (dialog.getPassport().jobSupported(GVTANMediaList.getLowlevelName())) {
            dialog.addTask(new GVTANMediaList(dialog.getPassport()));
            dialog.execute("HKTAB", true);
        }

        ScaMethodsResponse response = ScaMethodsResponse.builder()
            .tanTransportTypes(extractTanTransportTypes(dialog.getPassport()))
            .build();
        updateUpd(hbciCallback, response);
        return response;
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> request) {
        checkBankExists(request.getBank());
        BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request.getBank(), request);

        HBCIConsent hbciConsent = (HBCIConsent) request.getBankApiConsentData();

        try {
            if (hbciConsent.getStatus() == FINALISED) {
                SubmitAuthorisationCode<LoadAccounts> submitAuthorisationCode =
                    new SubmitAuthorisationCode<>(request);

                TransactionRequest<SubmitAuthorisationCode> submitAuthorisationCodeRequest =
                    hbciMapper.toSubmitAuthorisationCodeRequest(request, submitAuthorisationCode);

                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(submitAuthorisationCodeRequest);

                return (LoadAccountInformationResponse) submitAuthorizationCodeResponse.getJobResponse();
            } else {
                AccountInformationJob accountInformationJob = new AccountInformationJob(request);

                LoadAccountInformationResponse response = accountInformationJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            }
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public LoadBookingsResponse loadBookings(TransactionRequest<LoadBookings> request) {
        try {
            if (((HBCIConsent) request.getBankApiConsentData()).getStatus() == FINALISED) {
                SubmitAuthorisationCode<LoadBookings> submitAuthorisationCode = new SubmitAuthorisationCode<>(request);
                TransactionRequest<SubmitAuthorisationCode> submitAuthorisationCodeRequest =
                    hbciMapper.toSubmitAuthorisationCodeRequest(request, submitAuthorisationCode);

                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(submitAuthorisationCodeRequest);

                return (LoadBookingsResponse) submitAuthorizationCodeResponse.getJobResponse();
            } else {
                checkBankExists(request.getBank());
                BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request.getBank(), request);

                LoadBookingsJob loadBookingsJob = new LoadBookingsJob(request);
                LoadBookingsResponse response = loadBookingsJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            }

        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    public LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request) {
        checkBankExists(request.getBank());
        BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request.getBank(), request);

        HBCIConsent hbciConsent = (HBCIConsent) request.getBankApiConsentData();

        try {
            if (hbciConsent.getStatus() == FINALISED) {
                SubmitAuthorisationCode<LoadBalances> submitAuthorisationCode = new SubmitAuthorisationCode<>(request);

                TransactionRequest<SubmitAuthorisationCode> submitAuthorisationCodeRequest =
                    hbciMapper.toSubmitAuthorisationCodeRequest(request, submitAuthorisationCode);

                SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCodeResponse =
                    submitAuthorizationCode(submitAuthorisationCodeRequest);

                return (LoadBalancesResponse) submitAuthorizationCodeResponse.getJobResponse();
            } else {
                LoadBalancesJob loadBalancesJob = new LoadBalancesJob(request);
                LoadBalancesResponse response = loadBalancesJob.authorisationAwareExecute(hbciCallback);
                updateUpd(hbciCallback, response);
                return response;
            }

        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    public void executeTransactionWithoutSca(TransactionRequest<AbstractScaTransaction> request) {
        checkBankExists(request.getBank());
        setRequestBpdAndCreateCallback(request.getBank(), request);

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
        BpdUpdHbciCallback hbciCallback = setRequestBpdAndCreateCallback(request.getBank(), request);

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
    public SubmitAuthorizationCodeResponse<? extends AbstractResponse> submitAuthorizationCode(TransactionRequest<SubmitAuthorisationCode> request) {
        checkBankExists(request.getBank());
        setRequestBpdAndCreateCallback(request.getBank(), request);
        try {
            ScaRequiredJob scaJob = createScaJob(request);

            return new SubmitAuthorisationCodeJob<>(scaJob).sumbitAuthorizationCode(request);
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

    private BpdUpdHbciCallback setRequestBpdAndCreateCallback(Bank bank, AbstractRequest request) {
        String bankCode = bank.getBankApiBankCode() != null
            ? bank.getBankApiBankCode()
            : bank.getBankCode();

        return Optional.ofNullable(bpdCache)
            .map(cache -> {
                request.setHbciBPD(cache.get(bankCode));
                return new BpdUpdHbciCallback(bankCode, bpdCache);
            }).orElse(null);
    }

    private HBCIDialog createDialog(Bank bank, HbciDialogRequest dialogRequest) {
        checkBankExists(bank);

        String bankCode = bank.getBankApiBankCode() != null
            ? bank.getBankApiBankCode()
            : bank.getBankCode();

        Optional.ofNullable(bpdCache)
            .ifPresent(cache -> dialogRequest.setHbciBPD(cache.get(bankCode)));
        try {
            return HbciDialogFactory.startHbciDialog(null, dialogRequest);
        } catch (HBCI_Exception e) {
            throw handleHbciException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        BankInfo bankInfo = HBCIUtils.getBankInfo(bankCode);
        return bankInfo != null && bankInfo.getPinTanVersion() != null;
    }

    @SuppressWarnings("unchecked")
    private ScaRequiredJob createScaJob(TransactionRequest<SubmitAuthorisationCode> transactionRequest) {
        switch (transactionRequest.getTransaction().getTransactionType()) {
            case SINGLE_PAYMENT:
            case FUTURE_SINGLE_PAYMENT:
                return new SinglePaymentJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case FOREIGN_PAYMENT:
                return new ForeignPaymentJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case BULK_PAYMENT:
            case FUTURE_BULK_PAYMENT:
                return new BulkPaymentJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case STANDING_ORDER:
                return new NewStandingOrderJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case RAW_SEPA:
                return new RawSepaJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case FUTURE_SINGLE_PAYMENT_DELETE:
                return new DeleteFutureSinglePaymentJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case FUTURE_BULK_PAYMENT_DELETE:
                return new DeleteFutureBulkPaymentJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case STANDING_ORDER_DELETE:
                return new DeleteStandingOrderJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case TAN_REQUEST:
                return new TanRequestJob(transactionRequest);
            case LOAD_BANKACCOUNTS:
                return new AccountInformationJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case LOAD_BALANCES:
                return new LoadBalancesJob(transactionRequest.getTransaction().getOriginTransactionRequest());
            case LOAD_TRANSACTIONS:
                return new LoadBookingsJob(transactionRequest.getTransaction().getOriginTransactionRequest());
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

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable() {

            @Override
            public CreateConsentResponse createConsent(Consent consent, boolean redirectPreferred,
                                                       String tppRedirectUri) {
                HBCIConsent hbciConsent = new HBCIConsent();
                hbciConsent.setStatus(STARTED);

                return hbciMapper.toCreateConsentResponse(hbciConsent);
            }

            @Override
            public Consent getConsent(String consentId) {
                Consent consent = new Consent();
                consent.setConsentId(consentId);
                return consent;
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                HBCIConsent hbciConsent = (HBCIConsent) updatePsuAuthentication.getBankApiConsentData();

                AuthenticatePsuRequest request = hbciMapper.toAuthenticatePsuRequest(updatePsuAuthentication);

                ScaMethodsResponse response = authenticatePsu(request);
                hbciConsent.setTanMethodList(response.getTanTransportTypes());
                hbciConsent.setStatus(ScaStatus.PSUAUTHENTICATED);
                hbciConsent.setCredentials(request.getCredentials());

                return hbciMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                HBCIConsent hbciConsent = (HBCIConsent) transactionAuthorisation.getBankApiConsentData();
                hbciConsent.setStatus(ScaStatus.FINALISED);
                hbciConsent.setScaAuthenticationData(transactionAuthorisation.getScaAuthenticationData());

                return hbciMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                HBCIConsent hbciConsent = (HBCIConsent) selectPsuAuthenticationMethod.getBankApiConsentData();

                TanTransportType selectedMethod = hbciConsent.getTanMethodList().stream()
                    .filter(tanTransportType -> tanTransportType.getId().equals(selectPsuAuthenticationMethod.getAuthenticationMethodId()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
                hbciConsent.setSelectedMethod(selectedMethod);
                hbciConsent.setStatus(ScaStatus.SCAMETHODSELECTED);

                return hbciMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public void revokeConsent(String consentId) {
                //noop
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                             Object bankApiConsentData) {
                HBCIConsent hbciConsent = (HBCIConsent) bankApiConsentData;
                return hbciMapper.toUpdateAuthResponse(hbciConsent, bankApi());
            }

            @Override
            public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                        Object bankApiConsentData) {
                HBCIConsent hbciConsent = Optional.ofNullable(bankApiConsentData)
                    .map(o -> (HBCIConsent) o)
                    .orElseThrow(() -> new MultibankingException(MultibankingError.NO_CONSENT));

                if (hbciConsent.getStatus() != expectedConsentStatus) {
                    throw new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS);
                }
            }

            @Override
            public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
                //tansubmit persistence fur further call
                HBCIConsent hbciConsent = (HBCIConsent) bankApiConsentData;
                hbciConsent.setHbciTanSubmit(authorisationCodeResponse.getTanSubmit());
            }
        };
    }

    @RequiredArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class BpdUpdHbciCallback extends HbciCallback {

        private final String bankCode;
        private final Map<String, Map<String, String>> bpdCache;
        private Map<String, String> upd;
        private String sysId;

        @SuppressWarnings("unchecked")
        @Override
        public void status(int statusTag, Object o) {
            if (statusTag == STATUS_INST_BPD_INIT_DONE) {
                bpdCache.put(bankCode, (Map<String, String>) o);
            } else if (statusTag == STATUS_INIT_UPD_DONE) {
                this.upd = (Map<String, String>) o;
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
