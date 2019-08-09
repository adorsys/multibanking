package de.adorsys.multibanking.bg;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.banking_gateway_b2c.ApiClient;
import de.adorsys.multibanking.banking_gateway_b2c.ApiException;
import de.adorsys.multibanking.banking_gateway_b2c.api.BankingGatewayB2CAisApi;
import de.adorsys.multibanking.banking_gateway_b2c.model.ConsentTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.CreateConsentResponseTO;
import de.adorsys.multibanking.banking_gateway_b2c.model.ResourceUpdateAuthResponseTO;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_CONSENT;

@RequiredArgsConstructor
@Slf4j
public class BankingGatewayAdapter implements OnlineBankingService {

    private final String bankingGatewayBaseUrl;
    private BankingGatewayMapper bankingGatewayMapper = new BankingGatewayMapperImpl();

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
        //TODO xs2a adapter integration
//        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient
//        (loadAccountInformationRequest.getBankUrl()));
//
//        try {
//            AccountList accountList = ais.getAccountList(
//                loadAccountInformationRequest.getBankAccess().getBankCode(),
//                UUID.randomUUID(),
//                loadAccountInformationRequest.getConsentId(), false,
//                null, null, null,
//                null, PSU_IP_ADDRESS, null, null,
//                null, null,
//                null, null, null, null);
//
//            return LoadAccountInformationResponse.builder()
//                .bankAccounts(accountList.getAccounts()
//                    .stream()
//                    .map(BankingGatewayMapping::toBankAccount)
//                    .collect(Collectors.toList()))
//                .build();
//        } catch (ApiException e) {
//            throw handeAisApiException(e);
//        }
        return null;
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
        //TODO xs2a adapter integration
//        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(apiClient(loadBookingsRequest
//        .getBankUrl()));
//        String resourceId = loadBookingsRequest.getBankAccount().getExternalIdMap().get(BankApi.XS2A);
//        LocalDate dateFrom = loadBookingsRequest.getDateFrom() != null ? loadBookingsRequest.getDateFrom() :
//            LocalDate.now().minusYears(1);
//        LocalDate dateTo = loadBookingsRequest.getDateTo();
//        try {
//            TransactionsResponse200Json transactionList = ais.getTransactionList(
//                loadBookingsRequest.getBankAccess().getBankCode(),
//                resourceId, "booked", UUID.randomUUID(),
//                loadBookingsRequest.getAuthorisation(), dateFrom, dateTo, null, null,
//                null, null, null, null, null,
//                PSU_IP_ADDRESS, null, null, null,
//                null, null, null, null,
//                null);
//
//            return LoadBookingsResponse.builder()
//                .bookings(BankingGatewayMapping.toBookings(transactionList))
//                .build();
//
//        } catch (ApiException e) {
//            throw handeAisApiException(e);
//        }
        return null;
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
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable() {
            @Override
            public CreateConsentResponse createConsent(Consent consentTemplate) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    CreateConsentResponseTO consentResponse =
                        bankingGatewayB2CAisApi.createConsentUsingPOST(bankingGatewayMapper.toConsentTO(consentTemplate), null);

                    return bankingGatewayMapper.toCreateConsentResponse(consentResponse);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public Consent getConsent(String consentId) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    ConsentTO consentTO = bankingGatewayB2CAisApi.getConsentUsingGET(consentId);

                    return bankingGatewayMapper.toConsent(consentTO);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        bankingGatewayB2CAisApi.updatePsuAuthenticationUsingPUT(bankingGatewayMapper.toUpdatePsuAuthenticationRequestTO(updatePsuAuthentication),  updatePsuAuthentication.getAuthorisationId(), updatePsuAuthentication.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        bankingGatewayB2CAisApi.selectPsuAuthenticationMethodUsingPUT(bankingGatewayMapper.toSelectPsuAuthenticationMethodRequestTO(selectPsuAuthenticationMethod), selectPsuAuthenticationMethod.getAuthorisationId(), selectPsuAuthenticationMethod.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        bankingGatewayB2CAisApi.transactionAuthorisationUsingPUT(bankingGatewayMapper.toTransactionAuthorisationRequestTO(transactionAuthorisation), transactionAuthorisation.getAuthorisationId(), transactionAuthorisation.getConsentId());

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId) {
                try {
                    BankingGatewayB2CAisApi bankingGatewayB2CAisApi = new BankingGatewayB2CAisApi(apiClient());
                    ResourceUpdateAuthResponseTO resourceUpdateAuthResponseTO =
                        bankingGatewayB2CAisApi.getConsentAuthorisationStatusUsingGET(authorisationId, consentId);

                    return bankingGatewayMapper.toUpdateAuthResponseTO(resourceUpdateAuthResponseTO);
                } catch (ApiException e) {
                    throw handeAisApiException(e);
                }
            }

            @Override
            public void revokeConsent(String consentId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void validateConsent(String consentId) throws MultibankingException {
//                Consent consent = getConsent(consentId);
//                switch (consent.getScaStatus()) {
//                    case PARTIALLY_AUTHORISED:
//                        throw new MultibankingException(MultibankingError.INVALID_PIN);
//                    case PSU_AUTHORISED:
//                        throw new MultibankingException(MultibankingError.INVALID_SCA_METHOD);
//                    case SCA_METHOD_SELECTED:
//                        throw new MultibankingException(MultibankingError.INVALID_TAN);
//                    default:
//                        return;
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
        try {
            switch (e.getCode()) {
                case 400:
                    return handleAis400Error(e);
                case 401:
                    return handleAis401Error(e);
                case 429:
                    return new MultibankingException(INVALID_CONSENT, "consent access exceeded");
                default:
                    throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
            }
        } catch (IOException ex) {
            log.warn("unable to deserialize ApiException", ex);
        }

        throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }

    private MultibankingException handleAis401Error(ApiException e) throws IOException {
//        for (TppMessage401AIS tppMessage :
//            (objectMapper.readValue(e.getResponseBody(), Error401NGAIS.class)).getTppMessages()) {
//            if (tppMessage.getCode() == CONSENT_INVALID) {
//                return new MultibankingException(INVALID_AUTHORISATION, tppMessage.getText());
//            }
//        }
        return new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }

    private MultibankingException handleAis400Error(ApiException e) throws IOException {
//        for (TppMessage400AIS tppMessage :
//            (objectMapper.readValue(e.getResponseBody(), Error400NGAIS.class)).getTppMessages()) {
//            if (tppMessage.getCode() == CONSENT_UNKNOWN) {
//                return new MultibankingException(INVALID_AUTHORISATION, tppMessage.getText());
//            }
//        }
        return new MultibankingException(INTERNAL_ERROR, e.getMessage());
    }
}

