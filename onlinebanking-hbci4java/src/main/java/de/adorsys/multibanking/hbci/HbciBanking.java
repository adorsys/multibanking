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
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.hbci.job.*;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import de.adorsys.multibanking.hbci.util.HbciErrorUtils;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIProduct;
import org.kapott.hbci.manager.HBCIUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;

public class HbciBanking implements OnlineBankingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HbciBpdCacheHolder hbciBpdCacheHolder;
    private final HbciScaHandler hbciScaHandler;

    private final long sysIdMaxAgeMs;
    private final long updMaxAgeMs;

    public HbciBanking(HBCIProduct hbciProduct, long sysIdMaxAgeMs, long updMaxAgeMs, long bpdMaxAgeMs) {
        this(hbciProduct, null, sysIdMaxAgeMs, updMaxAgeMs, bpdMaxAgeMs);
    }

    public HbciBanking(HBCIProduct hbciProduct, InputStream customBankConfigInput, long sysIdMaxAgeMs, long updMaxAgeMs, long bpdMaxAgeMs) {
        this.hbciBpdCacheHolder = new HbciBpdCacheHolder(bpdMaxAgeMs);
        this.hbciScaHandler = new HbciScaHandler(hbciProduct, sysIdMaxAgeMs, updMaxAgeMs, hbciBpdCacheHolder);

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

        this.sysIdMaxAgeMs = sysIdMaxAgeMs;
        this.updMaxAgeMs = updMaxAgeMs;
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

    @Override
    public AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(request.getBank(), hbciBpdCacheHolder);

                AccountInformationJob accountInformationJob = new AccountInformationJob(request, hbciBpdCacheHolder);
                AccountInformationResponse response = accountInformationJob.execute(hbciCallback);
                response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisationResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                hbciConsent.afterTransactionAuthorisation(transactionAuthorisationResponse.getScaStatus());

                return (AccountInformationResponse) transactionAuthorisationResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadTransactionsRequest) {
        HbciConsent hbciConsent = (HbciConsent) loadTransactionsRequest.getBankApiConsentData();
        hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(loadTransactionsRequest.getBank(), hbciBpdCacheHolder);

                LoadTransactionsJob loadBookingsJob = new LoadTransactionsJob(loadTransactionsRequest, hbciBpdCacheHolder);
                TransactionsResponse response = loadBookingsJob.execute(hbciCallback);
                response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisationResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(loadTransactionsRequest));

                hbciConsent.afterTransactionAuthorisation(transactionAuthorisationResponse.getScaStatus());

                return (TransactionsResponse) transactionAuthorisationResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public StandingOrdersResponse loadStandingOrders(TransactionRequest<LoadStandingOrders> loadStandingOrdersRequest) {
        HbciConsent hbciConsent = (HbciConsent) loadStandingOrdersRequest.getBankApiConsentData();
        hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(loadStandingOrdersRequest.getBank(), hbciBpdCacheHolder);

                LoadStandingOrdersJob loadStandingOrdersJob = new LoadStandingOrdersJob(loadStandingOrdersRequest, hbciBpdCacheHolder);
                StandingOrdersResponse response = loadStandingOrdersJob.execute(hbciCallback);
                response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisationResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(loadStandingOrdersRequest));

                hbciConsent.afterTransactionAuthorisation(transactionAuthorisationResponse.getScaStatus());

                return (StandingOrdersResponse) transactionAuthorisationResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(request.getBank(), hbciBpdCacheHolder);

                LoadBalancesJob loadBalancesJob = new LoadBalancesJob(request, hbciBpdCacheHolder);
                LoadBalancesResponse response = loadBalancesJob.execute(hbciCallback);
                response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));
                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisationResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                hbciConsent.afterTransactionAuthorisation(transactionAuthorisationResponse.getScaStatus());

                return (LoadBalancesResponse) transactionAuthorisationResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public PaymentResponse executePayment(TransactionRequest<? extends AbstractPayment> request) {
        HbciConsent hbciConsent = (HbciConsent) request.getBankApiConsentData();
        hbciConsent.checkUpdSysIdCache(sysIdMaxAgeMs, updMaxAgeMs);

        try {
            if (hbciConsent.getHbciTanSubmit() == null || hbciConsent.getStatus() == FINALISED) {
                HbciBpdUpdCallback hbciCallback = HbciBpdUpdCallback.createCallback(request.getBank(), hbciBpdCacheHolder);

                ScaAwareJob<? extends AbstractPayment, PaymentResponse> paymentJob = createScaJob(request);

                PaymentResponse response = paymentJob.execute(hbciCallback);
                response.setBankApiConsentData(hbciCallback.updateConsentUpd(hbciConsent));

                return response;
            } else {
                TransactionAuthorisationResponse<? extends AbstractResponse> transactionAuthorisationResponse =
                    transactionAuthorisation(new TransactionAuthorisation<>(request));

                hbciConsent.afterTransactionAuthorisation(transactionAuthorisationResponse.getScaStatus());

                return (PaymentResponse) transactionAuthorisationResponse.getJobResponse();
            }
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return hbciScaHandler;
    }

    private <T extends AbstractTransaction, R extends AbstractResponse> TransactionAuthorisationResponse<R> transactionAuthorisation(TransactionAuthorisation<T> transactionAuthorisation) {
        try {
            ScaAwareJob<T, R> scaJob = createScaJob(transactionAuthorisation.getOriginTransactionRequest());

            TransactionAuthorisationJob<T, R> transactionAuthorisationJob = new TransactionAuthorisationJob<>(scaJob, transactionAuthorisation);
            TransactionAuthorisationResponse<R> response = transactionAuthorisationJob.execute();

            HbciConsent hbciConsent = ((HbciConsent) transactionAuthorisation.getOriginTransactionRequest().getBankApiConsentData());
            hbciConsent.afterTransactionAuthorisation(response.getScaStatus());

            return response;
        } catch (HBCI_Exception e) {
            throw HbciErrorUtils.handleHbciException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //not needed
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return Optional.ofNullable(HBCIUtils.getBankInfo(bankCode))
            .map(bankInfo -> bankInfo.getPinTanVersion() != null)
            .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractTransaction, R extends AbstractResponse> ScaAwareJob<T, R> createScaJob(TransactionRequest<T> transactionRequest) {
        switch (transactionRequest.getTransaction().getTransactionType()) {
            case SINGLE_PAYMENT:
            case FUTURE_SINGLE_PAYMENT:
            case INSTANT_PAYMENT:
                return (ScaAwareJob<T, R>) new SinglePaymentJob((TransactionRequest<SinglePayment>) transactionRequest, hbciBpdCacheHolder);
            case TRANSFER_PAYMENT:
                return (ScaAwareJob<T, R>) new TransferJob((TransactionRequest<SinglePayment>) transactionRequest, hbciBpdCacheHolder);
            case FOREIGN_PAYMENT:
                return (ScaAwareJob<T, R>) new ForeignPaymentJob((TransactionRequest<ForeignPayment>) transactionRequest, hbciBpdCacheHolder);
            case BULK_PAYMENT:
            case FUTURE_BULK_PAYMENT:
                return (ScaAwareJob<T, R>) new BulkPaymentJob((TransactionRequest<BulkPayment>) transactionRequest, hbciBpdCacheHolder);
            case STANDING_ORDER:
                return (ScaAwareJob<T, R>) new PeriodicPaymentJob((TransactionRequest<PeriodicPayment>) transactionRequest, hbciBpdCacheHolder);
            case RAW_SEPA:
                return (ScaAwareJob<T, R>) new RawSepaJob((TransactionRequest<RawSepaPayment>) transactionRequest, hbciBpdCacheHolder);
            case FUTURE_SINGLE_PAYMENT_DELETE:
                return (ScaAwareJob<T, R>) new DeleteFutureSinglePaymentJob((TransactionRequest<FutureSinglePayment>) transactionRequest, hbciBpdCacheHolder);
            case FUTURE_BULK_PAYMENT_DELETE:
                return (ScaAwareJob<T, R>) new DeleteFutureBulkPaymentJob((TransactionRequest<FutureBulkPayment>) transactionRequest, hbciBpdCacheHolder);
            case STANDING_ORDER_DELETE:
                return (ScaAwareJob<T, R>) new DeleteStandingOrderJob((TransactionRequest<PeriodicPayment>) transactionRequest, hbciBpdCacheHolder);
            case TAN_REQUEST:
                return (ScaAwareJob<T, R>) new TanRequestJob((TransactionRequest<TanRequest>) transactionRequest, hbciBpdCacheHolder);
            case LOAD_BANKACCOUNTS:
                return (ScaAwareJob<T, R>) new AccountInformationJob((TransactionRequest<LoadAccounts>) transactionRequest, hbciBpdCacheHolder);
            case LOAD_BALANCES:
                return (ScaAwareJob<T, R>) new LoadBalancesJob((TransactionRequest<LoadBalances>) transactionRequest, hbciBpdCacheHolder);
            case LOAD_TRANSACTIONS:
                return (ScaAwareJob<T, R>) new LoadTransactionsJob((TransactionRequest<LoadTransactions>) transactionRequest, hbciBpdCacheHolder);
            case LOAD_STANDING_ORDERS:
                return (ScaAwareJob<T, R>) new LoadStandingOrdersJob((TransactionRequest<LoadStandingOrders>) transactionRequest, hbciBpdCacheHolder);
            case GET_PAYMENT_STATUS:
                return (ScaAwareJob<T, R>) new InstantPaymentStatusJob((TransactionRequest<PaymentStatusReqest>) transactionRequest, hbciBpdCacheHolder);
            default:
                throw new IllegalArgumentException("invalid transaction type " + transactionRequest.getTransaction().getTransactionType());
        }
    }
}
