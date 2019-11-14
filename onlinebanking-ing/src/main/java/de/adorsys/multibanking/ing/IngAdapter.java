package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.domain.transaction.LoadTransactions;
import de.adorsys.multibanking.ing.api.Balance;
import de.adorsys.multibanking.ing.api.*;
import de.adorsys.multibanking.ing.http.ApacheHttpClient;
import de.adorsys.multibanking.ing.http.HttpClient;
import de.adorsys.multibanking.ing.http.Pkcs12KeyStore;
import de.adorsys.multibanking.ing.http.StringUri;
import de.adorsys.multibanking.ing.model.Response;
import de.adorsys.multibanking.ing.oauth.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;

import static de.adorsys.multibanking.domain.BankApi.ING;
import static de.adorsys.multibanking.domain.ScaStatus.STARTED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.*;
import static de.adorsys.multibanking.ing.http.ResponseHandlers.jsonResponseHandler;

@RequiredArgsConstructor
@Slf4j
public class IngAdapter implements OnlineBankingService {

    private static final String ACCOUNTS_ENDPOINT = "/v2/accounts";
    private static final String TRANSACTIONS_ENDPOINT = "/v2/accounts/{{accountId}}/transactions";
    private static final String BALANCES_ENDPOINT = "/v3/accounts/{{accountId}}/balances";

    @NonNull
    private final String ingBaseUrl;
    @NonNull
    private final String keystoreUrl;
    @NonNull
    private final String keystorePassword;
    @NonNull
    private final String qwacAlias;
    @NonNull
    private final String qsealAlias;

    @Getter(lazy = true)
    private final Pkcs12KeyStore keyStore = createKeyStore();
    @Getter(lazy = true)
    private final HttpClient httpClient = createHttpClient();
    @Getter(lazy = true)
    private final IngOauth2Service oauth2Service = createOauthService();

    private IngMapper ingMapper = new IngMapperImpl();

    @Override
    public BankApi bankApi() {
        return ING;
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
    public AccountInformationResponse loadBankAccounts(TransactionRequest<LoadAccounts> request) {
        IngSessionData ingSessionData = (IngSessionData) request.getBankApiConsentData();
        checkIngSession((IngSessionData) request.getBankApiConsentData(), request.getAuthorisationCode());

        ClientAuthentication clientAuthentication =
            getOauth2Service().getClientAuthentication(ingSessionData.getAccessToken());

        Response<AccountsResponse> response = getHttpClient().get(ingBaseUrl + ACCOUNTS_ENDPOINT)
            .send(clientAuthentication, jsonResponseHandler(AccountsResponse.class));

        return AccountInformationResponse.builder()
            .bankAccounts(ingMapper.toBankAccounts(response.getBody().getAccounts()))
            .build();
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        //noop
    }

    @Override
    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadBookingsRequest) {
        IngSessionData ingSessionData = (IngSessionData) loadBookingsRequest.getBankApiConsentData();
        checkIngSession(ingSessionData, loadBookingsRequest.getAuthorisationCode());

        ClientAuthentication clientAuthentication =
            getOauth2Service().getClientAuthentication(ingSessionData.getAccessToken());

        String resourceId =
            Optional.ofNullable(loadBookingsRequest.getTransaction().getPsuAccount().getExternalIdMap().get(bankApi()))
                .orElseGet(() -> getAccountResourceId(loadBookingsRequest.getBankAccess().getIban(),
                    clientAuthentication).toString());

        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("dateFrom", loadBookingsRequest.getTransaction().getDateFrom());
        queryParams.put("dateTo", loadBookingsRequest.getTransaction().getDateTo());

        String uri = StringUri.withQuery(
            ingBaseUrl + TRANSACTIONS_ENDPOINT.replace("{{accountId}}", Objects.requireNonNull(resourceId)),
            queryParams
        );

        Response<de.adorsys.multibanking.ing.api.TransactionsResponse> transactionsResponse = getHttpClient().get(uri)
            .send(clientAuthentication,
                jsonResponseHandler(de.adorsys.multibanking.ing.api.TransactionsResponse.class));

        return TransactionsResponse.builder()
            .bookings(ingMapper.mapToBookings(transactionsResponse.getBody().getTransactions().getBooked()))
            .balancesReport(getBalancesReport(clientAuthentication, resourceId))
            .build();
    }

    private BalancesReport getBalancesReport(ClientAuthentication clientAuthentication, String resourceId) {
        String uri = StringUri.withQuery(
            ingBaseUrl + BALANCES_ENDPOINT.replace("{{accountId}}", Objects.requireNonNull(resourceId)),
            new HashMap<>()
        );

        BalancesResponse balancesResponse = getHttpClient().get(uri)
            .send(clientAuthentication, jsonResponseHandler(BalancesResponse.class))
            .getBody();

        BalancesReport balancesReport = new BalancesReport();
        for (Balance balance : balancesResponse.getBalances()) {
            switch (balance.getBalanceType()) {
                case "EXPECTED":
                    balancesReport.setUnreadyBalance(ingMapper.toBalance(balance));
                    break;
                case "CLOSINGBOOKED":
                    balancesReport.setReadyBalance(ingMapper.toBalance(balance));
                    break;
                default:
                    // ignore
                    break;
            }
        }

        return balancesReport;
    }

    private UUID getAccountResourceId(String iban, ClientAuthentication clientAuthentication) {
        return getHttpClient().get(ingBaseUrl + ACCOUNTS_ENDPOINT)
            .send(clientAuthentication, jsonResponseHandler(AccountsResponse.class))
            .getBody().getAccounts()
            .stream()
            .filter(accountDetails -> accountDetails.getIban().equals(iban))
            .findAny()
            .map(Account::getResourceId)
            .orElseThrow(() -> new MultibankingException(INVALID_ACCOUNT_REFERENCE));
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
    public AbstractResponse executePayment(TransactionRequest<AbstractPayment> paymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return new StrongCustomerAuthorisable() {
            @Override
            public CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred,
                                                       String tppRedirectUri, Object bankApiConsentData) {
                URI authorizationRequestUri = getAuthorisationUri(tppRedirectUri);

                IngSessionData ingSessionData = new IngSessionData();
                ingSessionData.setStatus(STARTED);
                ingSessionData.setTppRedirectUri(tppRedirectUri);

                return ingMapper.toCreateConsentResponse(ingSessionData, authorizationRequestUri.toString());
            }

            @Override
            public Consent getConsent(String consentId) {
                return null;
            }

            @Override
            public UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisation) {
                throw new UnsupportedOperationException();
            }

            @Override
            public UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void revokeConsent(String consentId) {
                //noop
            }

            @Override
            public UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId,
                                                             Object bankApiConsentData) {
                IngSessionData ingSessionData = (IngSessionData) bankApiConsentData;
                return ingMapper.toUpdateAuthResponse(ingSessionData, bankApi());
            }

            @Override
            public void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                                        Object bankApiConsentData) {
                //noop
            }

            @Override
            public void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse) {
                //noop
            }

            @Override
            public void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode) {
                IngSessionData ingSessionData = (IngSessionData) bankApiConsentData;
                checkIngSession(ingSessionData, authorisationCode);
            }
        };
    }

    private URI getAuthorisationUri(String tppRedirectUri) {
        Oauth2Service.Parameters params = new Oauth2Service.Parameters(Collections.singletonMap("redirect_uri"
            , tppRedirectUri));
        return getOauth2Service().getAuthorizationRequestUri(params);
    }

    private void checkIngSession(IngSessionData ingSessionData, String authorisationCode) {
        TokenResponse tokenResponse = null;
        if (ingSessionData.getAccessToken() == null) {
            tokenResponse = Optional.ofNullable(authorisationCode)
                .map(this::getUserToken)
                .orElseThrow(() -> {
                    URI authorizationRequestUri = getAuthorisationUri(ingSessionData.getTppRedirectUri());
                    Message message = new Message();
                    message.setParamsMap(Collections.singletonMap("redirectUrl", authorizationRequestUri.toString()));
                    return new MultibankingException(MISSING_AUTHORISATION_CODE, 401,
                        Collections.singletonList(message));
                });
        } else if (LocalDateTime.now().isAfter(ingSessionData.getExpirationTime())) {
            tokenResponse = Optional.ofNullable(ingSessionData.getRefreshToken())
                .map(this::refreshToken)
                .orElseThrow(() -> {
                    URI authorizationRequestUri = getAuthorisationUri(ingSessionData.getTppRedirectUri());
                    Message message = new Message();
                    message.setParamsMap(Collections.singletonMap("redirectUrl", authorizationRequestUri.toString()));
                    return new MultibankingException(TOKEN_EXPIRED, 401, Collections.singletonList(message));
                });
        }

        Optional.ofNullable(tokenResponse)
            .ifPresent(response -> {
                ingSessionData.setAccessToken(response.getAccessToken());
                ingSessionData.setRefreshToken(response.getRefreshToken());
                ingSessionData.setExpirationTime(LocalDateTime.now().plusSeconds(response.getExpiresInSeconds()));
            });
    }

    private TokenResponse refreshToken(String refreshToken) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("grant_type", "refresh_token");
        parametersMap.put("refresh_token", refreshToken);

        return getOauth2Service().getToken(new Oauth2Service.Parameters(parametersMap));
    }

    private TokenResponse getUserToken(String authorisationCode) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("grant_type", "authorization_code");
        parametersMap.put("code", authorisationCode);

        return getOauth2Service().getToken(new Oauth2Service.Parameters(parametersMap));
    }

    private IngOauth2Service createOauthService() {
        try {
            Oauth2Api oauth2Api = new Oauth2Api(ingBaseUrl, getHttpClient());

            X509Certificate qsealCertificate = getKeyStore().getQsealCertificate(qsealAlias);
            PrivateKey qsealPrivateKey = getKeyStore().getQsealPrivateKey(qsealAlias);
            ClientAuthenticationFactory clientAuthenticationFactory = new ClientAuthenticationFactory(qsealCertificate,
                qsealPrivateKey);
            return new IngOauth2Service(oauth2Api, clientAuthenticationFactory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
        }
    }

    private ApacheHttpClient createHttpClient() {
        try {
            SSLContext sslContext = getKeyStore().getSslContext(qwacAlias);
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(socketFactory, null, null, (HostnameVerifier) null);

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().disableDefaultUserAgent();
            httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
            return new ApacheHttpClient(httpClientBuilder.build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
        }
    }

    private Pkcs12KeyStore createKeyStore() {
        try {
            return new Pkcs12KeyStore(new URL(keystoreUrl), keystorePassword.toCharArray());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MultibankingException(INTERNAL_ERROR, e.getMessage());
        }
    }
}

