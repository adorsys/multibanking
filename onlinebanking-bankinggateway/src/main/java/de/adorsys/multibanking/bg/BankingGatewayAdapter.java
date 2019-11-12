package de.adorsys.multibanking.bg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.ApiException;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.banking_gateway_b2c.model.*;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.domain.transaction.LoadTransactions;
import de.adorsys.multibanking.domain.transaction.TransactionAuthorisation;
import de.adorsys.multibanking.mapper.TransactionsParser;
import de.adorsys.xs2a.adapter.remote.api.AccountApi;
import de.adorsys.xs2a.adapter.remote.api.AccountInformationClient;
import de.adorsys.xs2a.adapter.mapper.TransactionsReportMapper;
import de.adorsys.xs2a.adapter.mapper.TransactionsReportMapperImpl;
import de.adorsys.xs2a.adapter.model.BookingStatusTO;
import de.adorsys.xs2a.adapter.model.PaymentProductTO;
import de.adorsys.xs2a.adapter.model.PaymentServiceTO;
import de.adorsys.xs2a.adapter.model.TransactionsResponse200JsonTO;
import de.adorsys.xs2a.adapter.remote.service.impl.RemoteAccountInformationService;
import de.adorsys.xs2a.adapter.service.*;
import de.adorsys.xs2a.adapter.service.model.AccountDetails;
import de.adorsys.xs2a.adapter.service.model.AccountListHolder;
import de.adorsys.xs2a.adapter.service.model.AccountReport;
import de.adorsys.xs2a.adapter.service.model.TransactionsReport;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.iban4j.Iban;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;

@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    @NonNull
    private final String bankingGatewayBaseUrl;
    @NonNull
    private final String xs2aAdapterBaseUrl;
    @Getter(lazy = true)
    private final BankingGatewayB2CAisApi bankingGatewayB2CAisApi = bankingGatewayB2CAisApi();
    @Getter(lazy = true)
    private final AccountInformationClient accountApi = Feign.builder()
        .requestInterceptor(new FeignCorrelationIdInterceptor())
        .contract(createSpringMvcContract())
        .logLevel(Logger.Level.FULL)
        .logger(new Slf4jLogger(AccountApi.class))
        .encoder(new JacksonEncoder())
        .decoder(new ResponseEntityDecoder(new StringDecoder(new JacksonDecoder())))
        .target(AccountInformationClient.class, xs2aAdapterBaseUrl);
    @Getter(lazy = true)
    private final AccountInformationService accountInformationService =
        new RemoteAccountInformationService(getAccountApi());
    private ObjectMapper objectMapper = new ObjectMapper();
    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();
    private TransactionsReportMapper transactionsReportMapper = new TransactionsReportMapperImpl();

    public BankingGatewayAdapter(String bankingGatewayBaseUrl, String xs2aAdapterBaseUrl) {
        this.bankingGatewayBaseUrl = bankingGatewayBaseUrl;
        this.xs2aAdapterBaseUrl = xs2aAdapterBaseUrl;

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private BankingGatewayB2CAisApi bankingGatewayB2CAisApi() {
        BankingGatewayB2CAisApi b2CAisApi = new BankingGatewayB2CAisApi(apiClient());
        b2CAisApi.getApiClient().getHttpClient().interceptors().clear();
        b2CAisApi.getApiClient().getHttpClient().interceptors().add(
            new HttpLoggingInterceptor(log::debug)
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        );
        b2CAisApi.getApiClient().getHttpClient().interceptors().add(new OkHttpCorrelationIdInterceptor());

        return b2CAisApi;
    }

    private SpringMvcContract createSpringMvcContract() {
        return new SpringMvcContract(Collections.emptyList(), new CustomConversionService());
    }

    @Override
    public BankApi bankApi() {
        return XS2A;
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
    public BankApiUser registerUser(String userId) {
        BankApiUser bankApiUser = new BankApiUser();
        bankApiUser.setBankApi(bankApi());
        return bankApiUser;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> loadAccountInformationRequest) {
        String token = Optional.ofNullable(loadAccountInformationRequest.getBankApiConsentData()).map(BgSessionData.class::cast).map(BgSessionData::getAccessToken).orElse(null);
        RequestHeaders aisHeaders = createAisHeaders(loadAccountInformationRequest, MediaType.APPLICATION_JSON_VALUE, token);

        Response<AccountListHolder> accountList = getAccountInformationService().getAccountList(aisHeaders,
            RequestParams.builder().build());

        List<BankAccount> bankAccounts = bankingGatewayMapper.toBankAccounts(accountList.getBody().getAccounts());

        return AccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(bankAccounts)
            .build();
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadBookingsRequest) {
        LoadTransactions loadBookings = loadBookingsRequest.getTransaction();
        String token = Optional.ofNullable(loadBookingsRequest.getBankApiConsentData()).map(BgSessionData.class::cast).map(BgSessionData::getAccessToken).orElse(null);


        String resourceId = Optional.ofNullable(loadBookings.getPsuAccount().getExternalIdMap().get(bankApi()))
            .orElseGet(() -> getAccountResourceId(loadBookingsRequest.getBankAccess().getIban(),
                createAisHeaders(loadBookingsRequest, MediaType.APPLICATION_JSON_VALUE, token)));

        RequestParams requestParams = RequestParams.builder()
            .dateFrom(loadBookings.getDateFrom() != null ? loadBookings.getDateFrom() : LocalDate.now().minusYears(1))
            .dateTo(loadBookings.getDateTo())
            .withBalance(loadBookings.isWithBalance())
            .bookingStatus(BookingStatusTO.BOOKED.toString()).build();

        try {
            RequestHeaders requestHeaders = createAisHeaders(loadBookingsRequest, MediaType.APPLICATION_XML_VALUE, token);
            Response<String> transactionListString =
                getAccountInformationService().getTransactionListAsString(resourceId, requestHeaders, requestParams);
            Map<String, String> headersMap = transactionListString.getHeaders().getHeadersMap();
            String contentType = headersMap.keySet().stream()
                .filter(header -> header.toLowerCase().contains("content-type"))
                .map(headersMap::get)
                .findFirst()
                .orElse("");
            String body = transactionListString.getBody();

            if (contentType.toLowerCase().contains("application/xml")) {
                return TransactionsParser.camtStringToLoadBookingsResponse(body);
            } else if (contentType.toLowerCase().contains("text/plain")) {
                return TransactionsParser.mt940StringToLoadBookingsResponse(body);
            } else {
                return jsonStringToLoadBookingsResponse(body);
            }
        } catch (FeignException e) {
            throw handeAisApiException(e);
        } catch (Exception e) {
            throw new MultibankingException(INTERNAL_ERROR, 500, "Error loading bookings: " + e.getMessage());
        }
    }

    private TransactionsResponse jsonStringToLoadBookingsResponse(String json) throws IOException {
        TransactionsResponse200JsonTO transactionsResponse200JsonTO = objectMapper.readValue(json,
            TransactionsResponse200JsonTO.class);
        TransactionsReport transactionList =
            transactionsReportMapper.toTransactionsReport(transactionsResponse200JsonTO);
        List<Booking> bookings = Optional.ofNullable(transactionList)
            .map(TransactionsReport::getTransactions)
            .map(AccountReport::getBooked)
            .map(transactions -> bankingGatewayMapper.toBookings(transactions))
            .orElse(Collections.emptyList());

        BalancesReport balancesReport = new BalancesReport();
        Optional.ofNullable(transactionList)
            .map(TransactionsReport::getBalances)
            .orElse(Collections.emptyList())
            .forEach(balance -> {
                switch (balance.getBalanceType()) {
                    case EXPECTED:
                        balancesReport.setUnreadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    case CLOSINGBOOKED:
                        balancesReport.setReadyBalance(bankingGatewayMapper.toBalance(balance));
                        break;
                    default:
                        // ignore
                        break;
                }
            });

        return TransactionsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }

    private String getAccountResourceId(String iban, RequestHeaders requestHeaders) {
        return getAccountInformationService().getAccountList(requestHeaders, RequestParams.builder().build())
            .getBody().getAccounts()
            .stream()
            .filter(accountDetails -> accountDetails.getIban().equals(iban))
            .findAny()
            .map(AccountDetails::getResourceId)
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE));
    }

    private RequestHeaders createAisHeaders(TransactionRequest transactionRequest, String mediaType, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_REQUEST_ID, UUID.randomUUID().toString());
        headers.put(RequestHeaders.CONSENT_ID, transactionRequest.getBankAccess().getConsentId());
        headers.put(RequestHeaders.X_GTW_BANK_CODE,
            transactionRequest.getBank().getBankApiBankCode() != null
                ? transactionRequest.getBank().getBankApiBankCode()
                : transactionRequest.getBankAccess().getBankCode());
        headers.put(RequestHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE); // TODO try camt
        Optional.ofNullable(bearerToken).ifPresent(token -> headers.put(RequestHeaders.AUTHORIZATION, String.format("Bearer %s", token)));
        return RequestHeaders.fromMap(headers);
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
    public TransactionAuthorisationResponse transactionAuthorisation(TransactionAuthorisation submitAuthorisationCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractResponse executePayment(TransactionRequest<AbstractPayment> paymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable() {
            @Override
            public CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred,
                                                       String tppRedirectUri) {
                try {
                    String bankCode = Iban.valueOf(consentTemplate.getPsuAccountIban()).getBankCode();
                    CreateConsentResponseTO consentResponse =
                        getBankingGatewayB2CAisApi().createConsentUsingPOST(bankingGatewayMapper.toConsentTO(consentTemplate), bankCode, null, null, redirectPreferred, tppRedirectUri);

                    BgSessionData sessionData = new BgSessionData();
                    sessionData.setConsentId(consentResponse.getConsentId());
                    CreateConsentResponse createConsentResponse = bankingGatewayMapper.toCreateConsentResponse(consentResponse);
                    createConsentResponse.setBankApiConsentData(sessionData);

                    return createConsentResponse;
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public Consent getConsent(String consentId) {
                try {
                    return bankingGatewayMapper.toConsent(getBankingGatewayB2CAisApi().getConsentUsingGET(consentId)); // TODO Bearer token
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                try {
                    UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequestTO =
                        bankingGatewayMapper.toUpdatePsuAuthenticationRequestTO(updatePsuAuthentication.getCredentials());
                    ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                        getBankingGatewayB2CAisApiSetToken(updatePsuAuthentication.getBankApiConsentData()).updatePsuAuthenticationUsingPUT(updatePsuAuthenticationRequestTO
                            , updatePsuAuthentication.getAuthorisationId(), updatePsuAuthentication.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponse, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                try {
                    ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                        getBankingGatewayB2CAisApiSetToken(selectPsuAuthenticationMethod.getBankApiConsentData()).selectPsuAuthenticationMethodUsingPUT(bankingGatewayMapper.toSelectPsuAuthenticationMethodRequestTO(selectPsuAuthenticationMethod), selectPsuAuthenticationMethod.getAuthorisationId(), selectPsuAuthenticationMethod.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponse, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                try {
                    ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                        getBankingGatewayB2CAisApiSetToken(transactionAuthorisation.getBankApiConsentData()).transactionAuthorisationUsingPUT(bankingGatewayMapper.toTransactionAuthorisationRequestTO(transactionAuthorisation), transactionAuthorisation.getAuthorisationId(), transactionAuthorisation.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponse, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                             Object bankApiConsentData) {
                try {
                    ResourceOfUpdateAuthResponseTO resourceUpdateAuthResponse =
                       getBankingGatewayB2CAisApiSetToken(bankApiConsentData).getConsentAuthorisationStatusUsingGET(authorisationId, consentId);

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponse, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void revokeConsent(String consentId) {
                try {
                    getBankingGatewayB2CAisApi().revokeConsentUsingDELETE(consentId); // TODO Bearer token
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                        Object bankApiConsentData) {
                try {
                    Optional.of(getBankingGatewayB2CAisApiSetToken(bankApiConsentData).getConsentAuthorisationStatusUsingGET(authorisationId,
                        consentId))
                        .map(consentStatus -> ScaStatus.valueOf(consentStatus.getScaStatus().getValue()))
                        .filter(consentStatus -> consentStatus == expectedConsentStatus)
                        .orElseThrow(() -> new MultibankingException(MultibankingError.INVALID_CONSENT_STATUS));
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
                //noop
            }

            private BankingGatewayB2CAisApi getBankingGatewayB2CAisApiSetToken(Object bankApiConsentData) {
                BankingGatewayB2CAisApi bankingGatewayB2CAisApi1 = getBankingGatewayB2CAisApi();
                Optional.ofNullable(bankApiConsentData).map(BgSessionData.class::cast).map(BgSessionData::getAccessToken)
                        .ifPresent(token -> bankingGatewayB2CAisApi1.getApiClient().setAccessToken(token));
                return bankingGatewayB2CAisApi1;
            }

            @Override
            public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
                BgSessionData sessionData = (BgSessionData) bankApiConsentData;
                String consentId = sessionData.getConsentId();

                try {
                    AuthorizationCodeTO authorizationCodeTO = new AuthorizationCodeTO();
                    authorizationCodeTO.setCode(authorisationCode);
                    OAuthToken token = getBankingGatewayB2CAisApi().resolveAuthCodeUsingPOST(authorizationCodeTO, consentId);

                    Optional.ofNullable(token)
                            .map(OAuthToken::getAccessToken)
                            .orElseThrow(() -> new MultibankingException(INTERNAL_ERROR, 500, "No bearer token received for auth code"));

                    sessionData.setAccessToken(token.getAccessToken());
                    sessionData.setRefreshToken(token.getRefreshToken());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }
        };
    }


    private ApiClient apiClient() {
        return apiClient(null, null);
    }

    private ApiClient apiClient(String acceptHeader, String contentTypeHeader) {
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
        apiClient.setBasePath(bankingGatewayBaseUrl);
        return apiClient;
    }

    private MultibankingException handeAisApiException(FeignException e) {
        if (e.status() == 429) {
            return new MultibankingException(CONSENT_ACCESS_EXCEEDED, 429, "consent access exceeded");
        }
        return new MultibankingException(BANKING_GATEWAY_ERROR, e.status(), e.getMessage());
    }

    private MultibankingException handeAisApiException(ApiException e) {
        switch (e.getCode()) {
            case 401:
                return toMultibankingException(e, INVALID_PIN);
            case 404:
                return toMultibankingException(e, RESOURCE_NOT_FOUND);
            case 400:
            case 500:
                return toMultibankingException(e, BANKING_GATEWAY_ERROR);
            case 429:
                return new MultibankingException(INVALID_CONSENT, 429, "consent access exceeded");
            default:
                throw new MultibankingException(BANKING_GATEWAY_ERROR, 500, e.getMessage());
        }
    }

    private MultibankingException toMultibankingException(ApiException e, MultibankingError multibankingError) {
        try {
            MessagesTO messagesTO = objectMapper.readValue(e.getResponseBody(), MessagesTO.class);
            return new MultibankingException(multibankingError, e.getCode(),
                bankingGatewayMapper.toMessages(messagesTO.getMessageList()));
        } catch (Exception e2) {
            return new MultibankingException(BANKING_GATEWAY_ERROR, 500, e.getMessage());
        }
    }

    public static class CustomConversionService extends DefaultConversionService {
        CustomConversionService() {
            addConverter(BookingStatusTO.class, String.class, BookingStatusTO::toString);
            addConverter(PaymentProductTO.class, String.class, PaymentProductTO::toString);
            addConverter(PaymentServiceTO.class, String.class, PaymentServiceTO::toString);
        }
    }

    @RequiredArgsConstructor
    public static class StringDecoder implements Decoder {
        @NonNull
        private final Decoder delegate;

        @Override
        public Object decode(feign.Response response, Type type) throws IOException {
            if (String.class.getName().equals(type.getTypeName())) {
                feign.Response.Body body = response.body();
                return IOUtils.toString(body.asInputStream());
            }
            return delegate.decode(response, type);
        }
    }
}

