package de.adorsys.multibanking.bg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.bg.api.AccountInformationServiceAisApi;
import de.adorsys.multibanking.bg.domain.Consent;
import static de.adorsys.multibanking.bg.domain.ConsentStatus.*;

import de.adorsys.multibanking.bg.exception.ConsentAuthorisationRequiredException;
import de.adorsys.multibanking.bg.exception.ConsentRequiredException;
import de.adorsys.multibanking.bg.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisationContainer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.bg.model.MessageCode400AIS.CONSENT_UNKNOWN;
import static de.adorsys.multibanking.bg.model.MessageCode401AIS.CONSENT_INVALID;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_AUTHORISATION;

@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BankApi bankApi() {
        return BankApi.BANKING_GATEWAY;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return true;
    }

    @Override
    public BankApiUser registerUser(BankAccess bankAccess, String pin) {
        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setBankApi(bankApi());
        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(loadAccountInformationRequest.getBankUrl()));

        try {
            AccountList accountList = ais.getAccountList(
                loadAccountInformationRequest.getBankAccess().getBankCode(),
                UUID.randomUUID(),
                loadAccountInformationRequest.getConsentId(), false,
                null, null, null,
                null, PSU_IP_ADDRESS, null, null,
                null, null,
                null, null, null, null);

            return LoadAccountInformationResponse.builder()
                .bankAccounts(accountList.getAccounts()
                    .stream()
                    .map(BankingGatewayMapping::toBankAccount)
                    .collect(Collectors.toList()))
                .build();
        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(loadBookingsRequest.getBankUrl()));
        String resourceId = loadBookingsRequest.getBankAccount().getExternalIdMap().get(BankApi.XS2A);
        LocalDate dateFrom = loadBookingsRequest.getDateFrom() != null ? loadBookingsRequest.getDateFrom() :
            LocalDate.now().minusYears(1);
        LocalDate dateTo = loadBookingsRequest.getDateTo();
        try {
            TransactionsResponse200Json transactionList = ais.getTransactionList(
                loadBookingsRequest.getBankAccess().getBankCode(),
                resourceId, "booked", UUID.randomUUID(),
                loadBookingsRequest.getAuthorisation(), dateFrom, dateTo, null, null,
                null, null, null, null, null,
                PSU_IP_ADDRESS, null, null, null,
                null, null, null, null,
                null);

            return LoadBookingsResponse.builder()
                .bookings(BankingGatewayMapping.toBookings(transactionList))
                .build();

        } catch (ApiException e) {
            throw handeAisApiException(e);
        }
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public AuthorisationCodeResponse requestAuthorizationCode(TransactionRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable<Consent> getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable<Consent>() {
            @Override
            public void containsValidAuthorisation(StrongCustomerAuthorisationContainer container) {
                // FIXME get consent id from access
                Object authorisation = container.getConsentId();
                if (authorisation == null || !(authorisation instanceof Consent)) {
                    throw new MissingAuthorisationException();
                }
                String consentId = ((Consent)authorisation).getConsentId();
                // FIXME get consent by banking gateway - maybe with a function here
                Consent consent = null;

                if (consent.getScaStatus() != VALID) {
                    if (consent.getScaStatus() == RECEIVED || consent.getScaStatus() == PARTIALLY_AUTHORISED) {
                        throw new ConsentAuthorisationRequiredException(consent);
                    } else {
                        throw new ConsentRequiredException();
                    }
                }
            }

            @Override
            public Consent createAuthorisation(Consent authorisation) {
                // FIXME create a authorisation by calling the banking gateway
                return null;
            }

            @Override
            public List<Consent> getAuthorisationList() {
                // for banking gateway there is only the creation of a consent available
                return null;
            }

            @Override
            public Consent getAuthorisation(String authorisationId) {
                // FIXME get consent from banking gateway?
                return null;
            }

            @Override
            public void revokeAuthorisation(String authorisationId) {
                // FIXME revoke consent at banking gateway?
            }
        };
    }

    private ApiClient apiClient(String url) {
        return apiClient(url, null, null);
    }

    private ApiClient apiClient(String url, String acceptHeader, String contentTypeHeader) {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(600, TimeUnit.SECONDS);
        client.interceptors().add(
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );

        ApiClient apiClient = new ApiClient() {
            @Override
            public String selectHeaderAccept(String[] accepts) {
                return Optional.ofNullable(acceptHeader)
                    .orElseGet(() -> super.selectHeaderAccept(accepts));
            }

            @Override
            public String selectHeaderContentType(String[] contentTypes) {
                return Optional.ofNullable(contentTypeHeader)
                    .orElseGet(() -> super.selectHeaderContentType(contentTypes));
            }

        };

        apiClient.setHttpClient(client);
        apiClient.setBasePath(url);
        return apiClient;
    }

    private MultibankingException handeAisApiException(ApiException e) {
        try {
            switch (e.getCode()) {
                case 400:
                    return handleAis400Error(e);
                case 401:
                    return handleAis401Error(e);
                case 429:
                    return new MultibankingException(INVALID_AUTHORISATION, "consent access exceeded");
                default:
                    throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
            }
        } catch (IOException ex) {
            log.warn("unable to deserialize ApiException", ex);
        }

        throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }

    private MultibankingException handleAis401Error(ApiException e) throws IOException {
        for (TppMessage401AIS tppMessage :
            (objectMapper.readValue(e.getResponseBody(), Error401NGAIS.class)).getTppMessages()) {
            if (tppMessage.getCode() == CONSENT_INVALID) {
                return new MultibankingException(INVALID_AUTHORISATION, tppMessage.getText());
            }
        }
        return new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }

    private MultibankingException handleAis400Error(ApiException e) throws IOException {
        for (TppMessage400AIS tppMessage :
            (objectMapper.readValue(e.getResponseBody(), Error400NGAIS.class)).getTppMessages()) {
            if (tppMessage.getCode() == CONSENT_UNKNOWN) {
                return new MultibankingException(INVALID_AUTHORISATION, tppMessage.getText());
            }
        }
        return new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }
}

