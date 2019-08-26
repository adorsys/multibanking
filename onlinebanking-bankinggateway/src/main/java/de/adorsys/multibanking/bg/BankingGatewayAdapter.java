package de.adorsys.multibanking.bg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.ApiException;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.banking_gateway_b2c.model.CreateConsentResponseTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.MessagesTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.ResourceUpdateAuthResponseTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.UpdatePsuAuthenticationRequestTO;
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
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.domain.transaction.LoadBookings;
import de.adorsys.multibanking.domain.transaction.SubmitAuthorisationCode;
import de.adorsys.xs2a.adapter.api.AccountApi;
import de.adorsys.xs2a.adapter.api.remote.AccountInformationClient;
import de.adorsys.xs2a.adapter.model.BookingStatusTO;
import de.adorsys.xs2a.adapter.model.PaymentProductTO;
import de.adorsys.xs2a.adapter.model.PaymentServiceTO;
import de.adorsys.xs2a.adapter.service.GeneralResponse;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.RequestParams;
import de.adorsys.xs2a.adapter.service.account.AccountDetails;
import de.adorsys.xs2a.adapter.service.account.AccountListHolder;
import de.adorsys.xs2a.adapter.service.account.TransactionsReport;
import de.adorsys.xs2a.adapter.service.ais.AccountInformationService;
import de.adorsys.xs2a.adapter.service.impl.AccountInformationServiceImpl;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.BankApi.XS2A;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;

@RequiredArgsConstructor
@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    @NonNull
    private final String bankingGatewayBaseUrl;
    @NonNull
    private final String xs2aAdapterBaseUrl;
    @Getter(lazy = true)
    private final BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
    @Getter(lazy = true)
    private final AccountInformationClient accountApi = Feign.builder()
        .contract(createSpringMvcContract())
        .logLevel(Logger.Level.FULL)
        .logger(new Slf4jLogger(AccountApi.class))
        .encoder(new JacksonEncoder())
        .decoder(new ResponseEntityDecoder(new JacksonDecoder()))
        .target(AccountInformationClient.class, xs2aAdapterBaseUrl);
    @Getter(lazy = true)
    private final AccountInformationService accountInformationService =
        new AccountInformationServiceImpl(getAccountApi());
    private ObjectMapper objectMapper = new ObjectMapper();
    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

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
    public LoadAccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> loadAccountInformationRequest) {
        RequestHeaders aisHeaders = createAisHeaders(loadAccountInformationRequest);

        GeneralResponse<AccountListHolder> accountList = getAccountInformationService().getAccountList(aisHeaders,
            RequestParams.builder().build());

        List<BankAccount> bankAccounts = accountList.getResponseBody().getAccounts().stream()
            .map(accountDetailsTO -> bankingGatewayMapper.toBankAccount(accountDetailsTO))
            .collect(Collectors.toList());

        return LoadAccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(bankAccounts)
            .build();
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public LoadBookingsResponse loadBookings(TransactionRequest<LoadBookings> loadBookingsRequest) {
        LoadBookings loadBookings = loadBookingsRequest.getTransaction();
        RequestHeaders requestHeaders = createAisHeaders(loadBookingsRequest);

        String resourceId = Optional.ofNullable(loadBookings.getPsuAccount().getExternalIdMap().get(bankApi()))
            .orElseGet(() -> getAccountResourceId(loadBookingsRequest.getBankAccess().getIban(), requestHeaders));

        RequestParams requestParams = RequestParams.builder()
            .dateFrom(loadBookings.getDateFrom())
            .dateTo(loadBookings.getDateTo())
            .withBalance(loadBookings.isWithBalance())
            .bookingStatus(BookingStatusTO.BOOKED.toString()).build();

        GeneralResponse<TransactionsReport> bookingsResponse =
            getAccountInformationService().getTransactionList(resourceId, requestHeaders, requestParams);

        List<Booking> bookings = bookingsResponse.getResponseBody().getTransactions().getBooked().stream()
            .map(transactions -> bankingGatewayMapper.toBooking(transactions))
            .collect(Collectors.toList());

        BalancesReport balancesReport = new BalancesReport();
        bookingsResponse.getResponseBody().getBalances().forEach(balance -> {
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

        return LoadBookingsResponse.builder()
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }

    private String getAccountResourceId(String iban, RequestHeaders requestHeaders) {
        return getAccountInformationService().getAccountList(requestHeaders, RequestParams.builder().build())
            .getResponseBody().getAccounts()
            .stream()
            .filter(accountDetails -> accountDetails.getIban().equals(iban))
            .findAny()
            .map(AccountDetails::getResourceId)
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE));
    }

    private RequestHeaders createAisHeaders(TransactionRequest transactionRequest) {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestHeaders.X_REQUEST_ID, UUID.randomUUID().toString());
        headers.put(RequestHeaders.CONSENT_ID, transactionRequest.getBankAccess().getConsentId());
        headers.put(RequestHeaders.X_GTW_BANK_CODE,
            transactionRequest.getBank().getBankApiBankCode() != null
                ? transactionRequest.getBank().getBankApiBankCode()
                : transactionRequest.getBankAccess().getBankCode());
        headers.put(RequestHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
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
    public <T extends AbstractResponse> SubmitAuthorizationCodeResponse submitAuthorizationCode(TransactionRequest<SubmitAuthorisationCode> submitPaymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorisationCodeResponse initiatePayment(TransactionRequest request) {
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

                    return bankingGatewayMapper.toCreateConsentResponse(consentResponse);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public Consent getConsent(String consentId) {
                try {
                    return bankingGatewayMapper.toConsent(getBankingGatewayB2CAisApi().getConsentUsingGET(consentId));
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                try {
                    UpdatePsuAuthenticationRequestTO updatePsuAuthenticationRequestTO =
                        bankingGatewayMapper.toUpdatePsuAuthenticationRequestTO(updatePsuAuthentication.getCredentials());
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        getBankingGatewayB2CAisApi().updatePsuAuthenticationUsingPUT(updatePsuAuthenticationRequestTO
                            , updatePsuAuthentication.getAuthorisationId(), updatePsuAuthentication.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                try {
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        getBankingGatewayB2CAisApi().selectPsuAuthenticationMethodUsingPUT(bankingGatewayMapper.toSelectPsuAuthenticationMethodRequestTO(selectPsuAuthenticationMethod), selectPsuAuthenticationMethod.getAuthorisationId(), selectPsuAuthenticationMethod.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                try {
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        getBankingGatewayB2CAisApi().transactionAuthorisationUsingPUT(bankingGatewayMapper.toTransactionAuthorisationRequestTO(transactionAuthorisation), transactionAuthorisation.getAuthorisationId(), transactionAuthorisation.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                             Object bankApiConsentData) {
                try {
                    ResourceUpdateAuthResponseTO updateAuthResponseTO =
                        getBankingGatewayB2CAisApi().getConsentAuthorisationStatusUsingGET(authorisationId, consentId);

                    return bankingGatewayMapper.toUpdateAuthResponseTO(updateAuthResponseTO, bankApi());
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void revokeConsent(String consentId) {
                try {
                    getBankingGatewayB2CAisApi().revokeConsentUsingDELETE(consentId);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                        Object bankApiConsentData) {
                try {
                    Optional.of(getBankingGatewayB2CAisApi().getConsentAuthorisationStatusUsingGET(authorisationId,
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

    private MultibankingException handeAisApiException(ApiException e) {
        switch (e.getCode()) {
            case 400:
            case 401:
                return handleBankingGatewayError(e);
            case 429:
                return new MultibankingException(INVALID_CONSENT, "consent access exceeded");
            default:
                throw new MultibankingException(BANKING_GATEWAY_ERROR, e.getMessage());
        }
    }

    private MultibankingException handleBankingGatewayError(ApiException e) {
        try {
            MessagesTO messagesTO = objectMapper.readValue(e.getResponseBody(), MessagesTO.class);
            return new MultibankingException(BANKING_GATEWAY_ERROR,
                bankingGatewayMapper.toMessages(messagesTO.getMessageList()));
        } catch (Exception e2) {
            return new MultibankingException(BANKING_GATEWAY_ERROR, e.getMessage());
        }
    }

    public static class CustomConversionService extends DefaultConversionService {

        CustomConversionService() {
            addConverter(BookingStatusTO.class, String.class, BookingStatusTO::toString);
            addConverter(PaymentProductTO.class, String.class, PaymentProductTO::toString);
            addConverter(PaymentServiceTO.class, String.class, PaymentServiceTO::toString);
        }
    }
}

