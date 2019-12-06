package de.adorsys.multibanking.ing;

import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.ing.api.Account;
import de.adorsys.multibanking.ing.api.AccountsResponse;
import de.adorsys.multibanking.ing.api.Balance;
import de.adorsys.multibanking.ing.api.BalancesResponse;
import de.adorsys.multibanking.ing.http.ApacheHttpClient;
import de.adorsys.multibanking.ing.http.HttpClient;
import de.adorsys.multibanking.ing.http.Pkcs12KeyStore;
import de.adorsys.multibanking.ing.http.StringUri;
import de.adorsys.multibanking.ing.model.Response;
import de.adorsys.multibanking.ing.oauth.ClientAuthentication;
import de.adorsys.multibanking.ing.oauth.ClientAuthenticationFactory;
import de.adorsys.multibanking.ing.oauth.IngOauth2Service;
import de.adorsys.multibanking.ing.oauth.Oauth2Api;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

import static de.adorsys.multibanking.domain.BankApi.ING;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INTERNAL_ERROR;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_ACCOUNT_REFERENCE;
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

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final Pkcs12KeyStore keyStore = createKeyStore();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final HttpClient httpClient = createHttpClient();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final IngOauth2Service oauth2Service = createOauthService();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final IngSessionHandler ingSessionHandler = new IngSessionHandler(getOauth2Service());
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final IngScaHandler ingScaHandler = new IngScaHandler(getIngSessionHandler());

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
        getIngSessionHandler().checkIngSession((IngSessionData) request.getBankApiConsentData(),
            request.getAuthorisationCode());

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
    public TransactionsResponse loadTransactions(TransactionRequest<LoadTransactions> loadTransactionsRequest) {
        IngSessionData ingSessionData = (IngSessionData) loadTransactionsRequest.getBankApiConsentData();
        getIngSessionHandler().checkIngSession(ingSessionData, loadTransactionsRequest.getAuthorisationCode());

        ClientAuthentication clientAuthentication =
            getOauth2Service().getClientAuthentication(ingSessionData.getAccessToken());

        String resourceId =
            Optional.ofNullable(loadTransactionsRequest.getTransaction().getPsuAccount().getExternalIdMap().get(bankApi()))
                .orElseGet(() -> getAccountResourceId(loadTransactionsRequest.getBankAccess().getIban(),
                    clientAuthentication).toString());

        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("dateFrom", loadTransactionsRequest.getTransaction().getDateFrom());
        queryParams.put("dateTo", loadTransactionsRequest.getTransaction().getDateTo());

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

    @Override
    public StandingOrdersResponse loadStandingOrders(TransactionRequest<LoadStandingOrders> loadStandingOrdersRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoadBalancesResponse loadBalances(TransactionRequest<LoadBalances> request) {
        throw new UnsupportedOperationException();
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
    public PaymentResponse executePayment(TransactionRequest<? extends AbstractPayment> paymentRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StrongCustomerAuthorisable getStrongCustomerAuthorisation() {
        return getIngScaHandler();
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

