package de.adorsys.multibanking.xs2a;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.*;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.xs2a.error.XS2AClientException;
import de.adorsys.multibanking.xs2a.executor.ConsentUpdateRequestExecutor;
import de.adorsys.multibanking.xs2a.executor.PaymentUpdateRequestExecutor;
import de.adorsys.multibanking.xs2a.executor.UpdateRequestExecutor;
import de.adorsys.multibanking.xs2a.model.XS2AUpdateRequest;
import de.adorsys.multibanking.xs2a.model.Xs2aTanSubmit;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.AccountInformationServiceAisApi;
import de.adorsys.psd2.client.api.PaymentInitiationServicePisApi;
import de.adorsys.psd2.client.model.AccountReference;
import de.adorsys.psd2.client.model.Balance;
import de.adorsys.psd2.client.model.*;
import de.adorsys.multibanking.xs2a.pis.PaymentInitiationBuilderStrategy;
import de.adorsys.multibanking.xs2a.pis.PaymentInitiationBuilderStrategyImpl;
import de.adorsys.multibanking.xs2a.pis.PaymentProductType;
import de.adorsys.multibanking.xs2a.pis.PaymentServiceType;
import domain.Xs2aBankApiUser;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.Iban;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.AbstractScaTransaction.TransactionType.DEDICATED_CONSENT;

public class XS2ABanking implements OnlineBankingService {

    public static final String PSU_IP_ADDRESS = "127.0.0.1";
    static final String SCA_AUTHENTICATION_METHOD_ID = "authenticationMethodId";
    static final String SCA_NAME = "name";
    static final String SCA_AUTHENTICATION_VERSION = "authenticationVersion";
    static final String SCA_EXPLANATION = "explanation";
    static final String SCA_METHODS = "scaMethods";
    static final String CHALLENGE_DATA = "data";
    static final String CHALLENGE_OTP_FORMAT = "otpFormat";
    static final String CHALLENGE_ADDITIONAL_INFORMATION = "additionalInformation";
    static final String CHALLENGE = "challengeData";
    private static final Logger logger = LoggerFactory.getLogger(XS2ABanking.class);
    private SSLSocketFactory sslSocketFactory;
    private PaymentInitiationBuilderStrategy initiationBuilderStrategy;

    public XS2ABanking() {
        this(defaultSslSocketFactory());
        initiationBuilderStrategy = new PaymentInitiationBuilderStrategyImpl();
    }

    public XS2ABanking(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    private static SSLSocketFactory defaultSslSocketFactory() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return sslContext.getSocketFactory();
    }

    private static BankAccount toBankAccount(AccountReference reference) {
        String iban = reference.getIban();
        BankAccount bankAccount = new BankAccount();
        bankAccount.setIban(iban);
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setBalances(new BalancesReport());
        return bankAccount;
    }

    @Override
    public BankApi bankApi() {
        return BankApi.XS2A;
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
    public BankApiUser registerUser(String bankingUrl, BankAccess bankAccess, String pin) {
        return null;
    }

    @Override
    public void removeUser(String bankingUrl, BankApiUser bankApiUser) {
    }

    @Override
    public ScaMethodsResponse authenticatePsu(String bankingUrl, AuthenticatePsuRequest authenticatePsuRequest) {
        ApiClient apiClient = createApiClient(bankingUrl);

        if (authenticatePsuRequest.getPaymentId() != null && authenticatePsuRequest.getConsentId() != null) {
            throw new IllegalArgumentException("Either payment or consent id should be set");
        }
        if (authenticatePsuRequest.getPaymentId() != null) {
            return authenticatePsuForPayment(authenticatePsuRequest, apiClient);
        }
        if (authenticatePsuRequest.getConsentId() != null) {
            return authenticatePsuForAccountInformationConsent(authenticatePsuRequest, apiClient);
        }
        throw new IllegalArgumentException("Neither payment nor consent id was set");
    }

    private ScaMethodsResponse authenticatePsuForPayment(AuthenticatePsuRequest authenticatePsuRequest,
                                                         ApiClient apiClient) {
        PaymentInitiationServicePisApi service = createPaymentInitiationServicePisApi(apiClient);

        UUID xRequestId = UUID.randomUUID();
        String paymentId = authenticatePsuRequest.getPaymentId();
        String corporateId = authenticatePsuRequest.getCustomerId();
        String psuId = authenticatePsuRequest.getLogin();
        String password = authenticatePsuRequest.getPin();
        UpdatePsuAuthentication psuBody = buildUpdatePsuAuthorisationBody(password);

        try {
            StartScaprocessResponse response;
            response = service.startPaymentAuthorisation(authenticatePsuRequest.getPaymentService(), authenticatePsuRequest.getPaymentProduct(), paymentId,
                                                         xRequestId, psuId,
                                                         null, null, null, null, null, null, PSU_IP_ADDRESS,
                                                         null, null, null, null, null, null, null, null, null);
            String authorisationId = getAuthorizationId(response);
            Map<String, Object> updatePsu = (Map<String, Object>) service.updatePaymentPsuData(authenticatePsuRequest.getPaymentService()
                    , authenticatePsuRequest.getPaymentProduct(), paymentId, authorisationId, xRequestId, psuBody,
                                                                                               null, null, null, psuId, null, corporateId,
                                                                                               null, PSU_IP_ADDRESS, null, null,
                                                                                               null, null, null,
                                                                                               null, null, null, null);
            return buildPsuAuthenticationResponse(updatePsu, authorisationId);
        } catch (ApiException e) {
            logger.error("Authorise PSU failed", e);
            throw new XS2AClientException(e);
        }
    }

    PaymentInitiationServicePisApi createPaymentInitiationServicePisApi(ApiClient apiClient) {
        return new PaymentInitiationServicePisApi(apiClient);
    }

    private ScaMethodsResponse authenticatePsuForAccountInformationConsent(
            AuthenticatePsuRequest authenticatePsuRequest, ApiClient apiClient) {
        AccountInformationServiceAisApi ais = createAccountInformationServiceAisApi(apiClient);
        String consentId = authenticatePsuRequest.getConsentId();
        String psuId = authenticatePsuRequest.getLogin();
        String corporatePsuId = authenticatePsuRequest.getCustomerId();
        StartScaprocessResponse startScaprocessResponse;
        try {
            startScaprocessResponse = ais.startConsentAuthorisation(consentId,
                    UUID.randomUUID(), null, null, null, psuId, null, corporatePsuId, null, PSU_IP_ADDRESS, null, null,
                    null, null, null, null, null, null, null);
        } catch (ApiException e) {
            logger.error("Failed to start consent authorisation", e);
            throw new XS2AClientException(e);
        }

        String authorisationId = getAuthorizationId(startScaprocessResponse);
        Object body = buildUpdatePsuAuthorisationBody(authenticatePsuRequest.getPin());

        Object response;
        try {
            response = ais.updateConsentsPsuData(consentId, authorisationId, UUID.randomUUID(), body, null, null, null,
                    psuId, null, corporatePsuId, null, PSU_IP_ADDRESS, null, null, null, null, null, null, null, null,
                    null);
        } catch (ApiException e) {
            logger.error("Failed to update consent authorisation", e);
            throw new XS2AClientException(e);
        }

        return buildPsuAuthenticationResponse((Map<String, Object>) response, authorisationId);
    }

    AccountInformationServiceAisApi createAccountInformationServiceAisApi(ApiClient apiClient) {
        return new AccountInformationServiceAisApi(apiClient);
    }

    private UpdatePsuAuthentication buildUpdatePsuAuthorisationBody(String password) {
        UpdatePsuAuthentication updatePsuAuthentication = new UpdatePsuAuthentication();
        updatePsuAuthentication.psuData(new PsuData()
                .password(password));
        return updatePsuAuthentication;
    }

    private String getAuthorizationId(StartScaprocessResponse response) {
        String link = (String) response.getLinks().get("startAuthorisationWithPsuAuthentication");
        if (StringUtils.isBlank(link)) {
            link = (String) response.getLinks().get("startAuthorisationWithPsuIdentification");
        }
        if (StringUtils.isNotBlank(link)) {
            int index = link.lastIndexOf('/') + 1;
            return link.substring(index);
        }
        throw new XS2AClientException("authorisation id was not found in the response");
    }

    private ScaMethodsResponse buildPsuAuthenticationResponse(Map<String, Object> response, String authorisationId) {
        List<TanTransportType> transportTypes = ((List<Map<String, String>>) response.getOrDefault(SCA_METHODS,
                Collections.EMPTY_LIST))
                .stream()
                .map(this::createTanType)
                .collect(Collectors.toList());
        return ScaMethodsResponse.builder()
                .authorizationId(authorisationId)
                .tanTransportTypes(transportTypes).build();
    }

    private TanTransportType createTanType(Map<String, String> map) {
        return new TanTransportType(map.get(SCA_AUTHENTICATION_METHOD_ID), map.get(SCA_NAME),
                map.get(SCA_AUTHENTICATION_VERSION), map.get(SCA_EXPLANATION));
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        String consentId = retrieveConsentId(loadAccountInformationRequest.getBankApiUser(), null);
        try {
            AccountList accountList = ais.getAccountList(UUID.randomUUID(), consentId, false,
                    null, null, null,
                    null, PSU_IP_ADDRESS, null, null,
                    null, null,
                    null, null, null, null);

            return LoadAccountInformationResponse.builder()
                    .bankAccounts(accountList.getAccounts()
                            .stream()
                            .map(XS2AMapping::toBankAccount)
                            .collect(Collectors.toList()))
                    .build();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeBankAccount(String bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser) {
    }

    @Override
    public LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        String iban = loadBookingsRequest.getBankAccount().getIban();
        String consentId = retrieveConsentId(loadBookingsRequest.getBankApiUser(), iban);

        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));
        String resourceId = loadBookingsRequest.getBankAccount().getExternalIdMap().get(BankApi.XS2A);
        LocalDate dateFrom = loadBookingsRequest.getDateFrom();
        LocalDate dateTo = loadBookingsRequest.getDateTo();
        try {
            TransactionsResponse200Json transactionList = ais.getTransactionList(
                    resourceId, "booked", UUID.randomUUID(),
                    consentId, dateFrom, dateTo, null, null,
                    null, null, null, null, null,
                    PSU_IP_ADDRESS, null, null, null,
                    null, null, null, null,
                    null);

            return LoadBookingsResponse.builder()
                    .bookings(XS2AMapping.toBookings(transactionList))
                    .build();

        } catch (ApiException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BankAccount> loadBalances(String bankingUrl, LoadBalanceRequest loadBalanceRequest) {
        List<BankAccount> bankAccounts = loadBalanceRequest.getBankAccounts();
        if (bankAccounts.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        if (bankAccounts.size() > 1) {
            logger.warn("Only first bank account will be processed");
        }
        //todo: load balances for list of accounts
        BankAccount account = bankAccounts.get(0);
        String accountId = account.getExternalIdMap().get(BankApi.XS2A);
        UUID xRequestId = UUID.randomUUID();

        String consentId = retrieveConsentId(loadBalanceRequest.getBankApiUser(), account.getIban());

        AccountInformationServiceAisApi ais = createAccountInformationServiceAisApi(createApiClient(bankingUrl));

        try {
            ReadAccountBalanceResponse200 balances = ais.getBalances(accountId, xRequestId, consentId, null,
                    null, null,
                    PSU_IP_ADDRESS, null, null,
                    null, null,
                    null, null,
                    null, null, null);
            return Collections.singletonList(convertToBankAccount(balances));
        } catch (ApiException e) {
            logger.error("Loading balances failed", e);
            throw new XS2AClientException(e);
        }
    }

    private String retrieveConsentId(BankApiUser bankApiUser, String iban) {
        Optional<String> consent;
        if (bankApiUser instanceof Xs2aBankApiUser) {
            consent = Optional.ofNullable(((Xs2aBankApiUser) bankApiUser).getConsentId());
        } else {
            // deprecated version
            consent = Optional.ofNullable(bankApiUser.getProperties().get("consentId-" + iban));
        }
        return consent.orElseThrow(() -> new MissingConsentException("missing consent for transactions request"));

    }

    private BankAccount convertToBankAccount(ReadAccountBalanceResponse200 balances) {
        BankAccount bankAccount = toBankAccount(balances.getAccount());
        BalancesReport balancesReport = bankAccount.getBalances();

        for (Balance balance : balances.getBalances()) {
            BalanceType balanceType = balance.getBalanceType();
            switch (balanceType) {
                case CLOSINGBOOKED:
                    balancesReport.setReadyBalance(toMultibankingBalance(balance));
                    break;
                case EXPECTED:
                    balancesReport.setUnreadyBalance(toMultibankingBalance(balance));
                    break;
                default:
                    logger.warn("Unexpected {} balance", balanceType);
            }
        }

        return bankAccount;
    }

    private de.adorsys.multibanking.domain.Balance toMultibankingBalance(Balance balance) {
        BigDecimal amount = new BigDecimal(balance.getBalanceAmount().getAmount());
        String currency = balance.getBalanceAmount().getCurrency();
        LocalDate referenceDate = balance.getReferenceDate();

        return new de.adorsys.multibanking.domain.Balance(referenceDate, amount, currency);
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
    public InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest) {
        UUID xRequestId = UUID.randomUUID();
        AbstractScaTransaction payment = paymentRequest.getTransaction();
        PaymentProductType paymentProduct = PaymentProductType.resolve(payment.getProduct());
        PaymentServiceType paymentService = PaymentServiceType.resolve(payment);
        String contentType = "application/" + (paymentProduct.isRaw() ? "xml" : "json");
        String psuId = paymentRequest.getBankAccess().getBankLogin();
        Object paymentBody;
        paymentBody = initiationBuilderStrategy.resolve(paymentProduct, paymentService).buildBody(payment);
        ApiClient apiClient = createApiClient(bankingUrl, contentType);
        PaymentInitiationServicePisApi initiationService = createPaymentInitiationServicePisApi(apiClient);

        try {
            Map<String, Object> response = (Map<String, Object>) initiationService.initiatePayment(
                    paymentBody,
                    paymentService.getType(),
                    paymentProduct.getType(),
                    xRequestId,
                    PSU_IP_ADDRESS,
                    null, null, null, psuId, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null);

            return getInitiatePaymentResponse(response);

        } catch (ApiException e) {
            logger.error("Initiate payment failed", e);
            throw new XS2AClientException(e);
        }
    }

    @Override
    public void executeTransactionWithoutSca(String bankingUrl, TransactionRequest paymentRequest) {
    }

    private InitiatePaymentResponse getInitiatePaymentResponse(Map<String, Object> response) {
        String transactionStatus = (String) response.get("transactionStatus");
        String paymentId = (String) response.get("paymentId");
        Map<String, String> links = (Map<String, String>) response.get("_links");
        return new InitiatePaymentResponse(transactionStatus, paymentId, links);
    }

    @Override
    public AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest request) {
        ApiClient apiClient = createApiClient(bankingUrl);
        if (request.getTransaction().getTransactionType() == DEDICATED_CONSENT) {
            return requestAuthorizationCodeForAccountInformationConsent(request, apiClient);
        }
        return requestAuthorizationCodeForPayment(bankingUrl, request, apiClient);
    }

    private AuthorisationCodeResponse requestAuthorizationCodeForPayment(String bankingUrl,
                                                                         TransactionRequest paymentRequest,
                                                                         ApiClient apiClient) {
        PaymentInitiationServicePisApi service = createPaymentInitiationServicePisApi(apiClient);

        AbstractScaTransaction payment = paymentRequest.getTransaction();
        PaymentProductType paymentProduct = PaymentProductType.resolve(payment.getProduct());
        PaymentServiceType paymentService = PaymentServiceType.resolve(payment);

        String paymentId = payment.getPaymentId();
        String authorisationId = paymentRequest.getAuthorisationId();

        UUID xRequestId = UUID.randomUUID();
        String psuId = paymentRequest.getBankAccess().getBankLogin();
        String corporateId = paymentRequest.getBankAccess().getBankLogin2();
        SelectPsuAuthenticationMethod body =
                buildSelectPsuAuthenticationMethod(paymentRequest.getTanTransportType().getId());
        Xs2aTanSubmit tanSubmit = new Xs2aTanSubmit(bankingUrl, paymentId, authorisationId, psuId, corporateId);
        tanSubmit.setPaymentProduct(paymentProduct.getType());
        tanSubmit.setPaymentService(paymentService.getType());

        try {
            Map<String, Object> updatePsuData =
                    (Map<String, Object>) service.updatePaymentPsuData(paymentService.getType(), paymentProduct.getType(),
                                                                       paymentId, authorisationId,
                                                                       xRequestId, body,
                                                                       null, null,
                                                                       null, psuId,
                                                                       null, corporateId,
                                                                       null, PSU_IP_ADDRESS,
                                                                       null, null,
                                                                       null, null,
                                                                       null, null,
                                                                       null, null, null);

            return buildAuthorisationCodeResponse(updatePsuData, tanSubmit);
        } catch (ApiException e) {
            logger.error("Initiate payment failed", e);
            throw new XS2AClientException(e);
        }
    }

    private AuthorisationCodeResponse buildAuthorisationCodeResponse(Map<String, Object> updatePsuData,
                                                                     Xs2aTanSubmit tanSubmit) {
        AuthorisationCodeResponse response = new AuthorisationCodeResponse();
        response.setTanSubmit(tanSubmit);
        Map<String, String> map = (Map<String, String>) updatePsuData.get(CHALLENGE);
        TanChallenge challenge = new TanChallenge();
        challenge.setData(map.get(CHALLENGE_DATA));
        challenge.setFormat(map.get(CHALLENGE_OTP_FORMAT));
        challenge.setTitle(map.get(CHALLENGE_ADDITIONAL_INFORMATION));
        response.setChallenge(challenge);
        return response;
    }

    private SelectPsuAuthenticationMethod buildSelectPsuAuthenticationMethod(String methodId) {
        SelectPsuAuthenticationMethod selectPsuAuthenticationMethod = new SelectPsuAuthenticationMethod();
        selectPsuAuthenticationMethod.setAuthenticationMethodId(methodId);
        return selectPsuAuthenticationMethod;
    }

    private AuthorisationCodeResponse requestAuthorizationCodeForAccountInformationConsent(TransactionRequest request,
                                                                                           ApiClient apiClient) {
        AccountInformationServiceAisApi ais = createAccountInformationServiceAisApi(apiClient);
        String consentId = request.getTransaction().getOrderId();
        String authorisationId = request.getAuthorisationId();
        Object body = buildSelectPsuAuthenticationMethod(request.getTanTransportType().getId());
        String psuId = request.getBankAccess().getBankLogin();
        String psuCorporateId = request.getBankAccess().getBankLogin2();
        Object response;
        try {
            response = ais.updateConsentsPsuData(consentId, authorisationId, UUID.randomUUID(), body, null, null, null,
                    psuId, null, psuCorporateId, null, PSU_IP_ADDRESS, null, null, null, null, null, null, null, null,
                    null);
        } catch (ApiException e) {
            logger.error("Failed to request authorization code", e);
            throw new XS2AClientException(e);
        }

        return buildAuthorisationCodeResponse((Map<String, Object>) response, new Xs2aTanSubmit(apiClient.getBasePath(),
                consentId, authorisationId, psuId, psuCorporateId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        Xs2aTanSubmit tanSubmit = (Xs2aTanSubmit) submitPaymentRequest.getTanSubmit();
        String bankingUrl = tanSubmit.getBankingUrl();
        ApiClient apiClient = createApiClient(bankingUrl);

        UpdateRequestExecutor executor = createUpdateRequestExecutor(submitPaymentRequest);
        XS2AUpdateRequest request = executor.buildRequest(submitPaymentRequest);
        try {
            return executor.execute(request, apiClient);
        } catch (ApiException e) {
            logger.error("Submit authorisation code failed", e);
            throw new XS2AClientException(e);
        }
    }

    UpdateRequestExecutor createUpdateRequestExecutor(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        UpdateRequestExecutor executor;
        if (submitPaymentRequest.getSepaTransaction().getTransactionType() == DEDICATED_CONSENT) {
            executor = new ConsentUpdateRequestExecutor();
        } else {
            executor = new PaymentUpdateRequestExecutor();
        }
        return executor;
    }

    @Override
    public boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference) {
        return !Optional.ofNullable(bankApiUser.getProperties().get("consentId-" + accountReference)).isPresent();
    }

    @Override
    public CreateConsentResponse createAccountInformationConsent(String bankingUrl, CreateConsentRequest request) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        Consents consents = toConsents(request);
        BankAccess bankAccess = request.getBankAccess();
        ConsentsResponse201 response;
        try {
            response = ais.createConsent(UUID.randomUUID(), consents, null, null, null, bankAccess.getBankLogin(), null,
                    bankAccess.getBankLogin2(), null, "false", null, null, null, PSU_IP_ADDRESS, null, null, null, null,
                    null, null, null, null, null);
        } catch (ApiException e) {
            logger.error("Create consent failed", e);
            throw new XS2AClientException(e);
        }

        // The consent object to be retrieved by the GET Consent Request will contain the adjusted date
        // NextGenPSD2 Access to Account Interoperability Framework - Implementation Guidelines V1.3_20181019.pdf
        // 6.4.1.1
        String consentId = response.getConsentId();
        ConsentInformationResponse200Json consentInformation;
        try {
            consentInformation = ais.getConsentInformation(consentId, UUID.randomUUID(), null, null, null,
                    PSU_IP_ADDRESS, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            logger.error("Get consent failed", e);
            throw new XS2AClientException(e);
        }

        @SuppressWarnings("unchecked")
        Map<String, String> links = response.getLinks();

        return CreateConsentResponse.builder()
                .consentId(consentId)
                .validUntil(consentInformation.getValidUntil())
                .links(links)
                .build();
    }

    private Consents toConsents(CreateConsentRequest request) {
        Consents consents = new Consents();
        consents.setAccess(toAccountAccess(request));
        consents.setRecurringIndicator(request.isRecurringIndicator());
        consents.setValidUntil(request.getValidUntil());
        consents.setFrequencyPerDay(request.getFrequencyPerDay());
        consents.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        return consents;
    }

    private AccountAccess toAccountAccess(CreateConsentRequest request) {
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setAccounts(toAccountReferences(request.getAccounts()));
        accountAccess.setBalances(toAccountReferences(request.getBalances()));
        accountAccess.setTransactions(toAccountReferences(request.getTransactions()));
        return accountAccess;
    }

    private List<AccountReference> toAccountReferences(List<de.adorsys.multibanking.domain.AccountReference> accounts) {
        ArrayList<AccountReference> accountReferences = new ArrayList<>();
        for (de.adorsys.multibanking.domain.AccountReference account : accounts) {
            AccountReference accountReference = new AccountReference();
            accountReference.setIban(account.getIban());
            accountReference.setCurrency(account.getCurrency());
            accountReferences.add(accountReference);
        }
        return accountReferences;
    }

    private ApiClient createApiClient(String bankingUrl, String contentType) {
        ApiClient apiClient = new ApiClient() {
            @Override
            public String selectHeaderContentType(String[] contentTypes) {
                return Optional.ofNullable(contentType)
                        .orElseGet(() -> super.selectHeaderContentType(contentTypes));
            }
        };

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(
                new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );
        client.setReadTimeout(600, TimeUnit.SECONDS);
        client.setSslSocketFactory(sslSocketFactory);
        apiClient.setHttpClient(client);
        Optional.ofNullable(bankingUrl)
                .ifPresent(url -> apiClient.setBasePath(url));

        return apiClient;
    }

    ApiClient createApiClient(String bankingUrl) {
        return createApiClient(bankingUrl, null);
    }

}
