package de.adorsys.xs2a;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.AccountInformationServiceAisApi;
import de.adorsys.psd2.client.api.PaymentInitiationServicePisApi;
import de.adorsys.psd2.client.model.AccountReference;
import de.adorsys.psd2.client.model.*;
import de.adorsys.xs2a.error.XS2AClientException;
import de.adorsys.xs2a.executor.ConsentUpdateRequestExecutor;
import de.adorsys.xs2a.executor.PaymentUpdateRequestExecutor;
import de.adorsys.xs2a.executor.UpdateRequestExecutor;
import de.adorsys.xs2a.model.XS2AUpdateRequest;
import de.adorsys.xs2a.model.Xs2aTanSubmit;
import domain.*;
import domain.request.*;
import domain.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spi.OnlineBankingService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static domain.AbstractScaTransaction.TransactionType.DEDICATED_CONSENT;

public class XS2ABanking implements OnlineBankingService {

    private static final Logger logger = LoggerFactory.getLogger(XS2ABanking.class);

    public static final String PSU_IP_ADDRESS = "127.0.0.1";
    public static final String SINGLE_PAYMENT_SERVICE = "payments";
    public static final String SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers";
    static final String SCA_AUTHENTICATION_METHOD_ID = "authenticationMethodId";
    static final String SCA_NAME = "name";
    static final String SCA_AUTHENTICATION_VERSION = "authenticationVersion";
    static final String SCA_EXPLANATION = "explanation";
    static final String SCA_METHODS = "scaMethods";
    static final String CHALLENGE_DATA = "data";
    static final String CHALLENGE_OTP_FORMAT = "otpFormat";
    static final String CHALLENGE_ADDITIONAL_INFORMATION = "additionalInformation";
    static final String CHALLENGE = "challengeData";

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
        return true;
    }

    @Override
    public BankApiUser registerUser(String bankingUrl, BankAccess bankAccess, String pin) {
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setAllPsd2(AccountAccess.AllPsd2Enum.ALLACCOUNTS);

        Consents consents = new Consents();
        consents.setValidUntil(LocalDate.now().plusDays(30));
        consents.setFrequencyPerDay(100);
        consents.setAccess(accountAccess);
        consents.setRecurringIndicator(true);

        try {
            BankApiUser bankApiUser = new BankApiUser();
            bankApiUser.setApiUserId(bankAccess.getBankLogin());
            bankApiUser.setBankApi(BankApi.XS2A);
            bankApiUser.setProperties(new HashMap<>());
            bankApiUser.getProperties().put("allAccountsConsentId",
                                            createConsent(bankingUrl, bankAccess, pin, consents).getConsentId());

            return bankApiUser;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private ScaMethodsResponse authenticatePsuForPayment(AuthenticatePsuRequest authenticatePsuRequest, ApiClient apiClient) {
        PaymentInitiationServicePisApi service = createPaymentInitiationServicePisApi(apiClient);

        UUID xRequestId = UUID.randomUUID();
        String paymentId = authenticatePsuRequest.getPaymentId();
        String corporateId = authenticatePsuRequest.getCustomerId();
        String psuId = authenticatePsuRequest.getLogin();
        String password = authenticatePsuRequest.getPin();
        UpdatePsuAuthentication psuBody = buildUpdatePsuAuthorisationBody(password);

        try {
            StartScaprocessResponse response;
            response = service.startPaymentAuthorisation(SINGLE_PAYMENT_SERVICE, SEPA_CREDIT_TRANSFERS, paymentId, xRequestId, psuId,
                                                         null, null, null, null, null, null, PSU_IP_ADDRESS,
                                                         null, null, null, null, null, null, null, null, null);
            String authorisationId = getAuthorizationId(response);
            Map<String, Object> updatePsu = (Map<String, Object>) service.updatePaymentPsuData(SINGLE_PAYMENT_SERVICE, SEPA_CREDIT_TRANSFERS, paymentId, authorisationId, xRequestId, psuBody,
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
        List<TanTransportType> transportTypes = ((List<Map<String, String>>) response.getOrDefault(SCA_METHODS, Collections.EMPTY_LIST))
                                                        .stream()
                                                        .map(this::createTanType)
                                                        .collect(Collectors.toList());
        return ScaMethodsResponse.builder()
                       .authorizationId(authorisationId)
                       .tanTransportTypes(transportTypes).build();
    }

    private TanTransportType createTanType(Map<String, String> map) {
        return new TanTransportType(map.get(SCA_AUTHENTICATION_METHOD_ID), map.get(SCA_NAME), map.get(SCA_AUTHENTICATION_VERSION), map.get(SCA_EXPLANATION));
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        String consentId = loadAccountInformationRequest.getBankApiUser().getProperties().get("allAccountsConsentId");
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
        String consentId =
                Optional.ofNullable(loadBookingsRequest.getBankApiUser().getProperties().get("consentId-" + loadBookingsRequest.getBankAccount().getIban()))
                        .orElseThrow(() -> new MissingConsentException("missing consent for transactions request"));

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
    public InitiatePaymentResponse initiatePayment(String bankingUrl, TransactionRequest paymentRequest) {
        UUID xRequestId = UUID.randomUUID();
        String paymentProduct;
        String contentType;
        String psuId = paymentRequest.getBankAccess().getBankLogin();
        Object paymentBody;

        Optional<String> rawData = Optional.ofNullable(paymentRequest.getTransaction().getRawData());
        if (rawData.isPresent()) {
            paymentBody = rawData.get().getBytes();
            paymentProduct = "pain.001-sepa-credit-transfers";
            contentType = "application/xml";
        } else {
            paymentBody = convertToPaymentInitiation(paymentRequest);
            paymentProduct = SEPA_CREDIT_TRANSFERS;
            contentType = "application/json";
        }
        ApiClient apiClient = createApiClient(bankingUrl, contentType);
        PaymentInitiationServicePisApi initiationService = createPaymentInitiationServicePisApi(apiClient);

        try {
            Map<String, Object> response = (Map<String, Object>) initiationService.initiatePayment(
                    paymentBody,
                    SINGLE_PAYMENT_SERVICE,
                    paymentProduct,
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

    //todo: replace by mapper
    private PaymentInitiationSctJson convertToPaymentInitiation(TransactionRequest paymentRequest) {
        SinglePayment paymentBodyObj = (SinglePayment) paymentRequest.getTransaction();
        PaymentInitiationSctJson paymentInitiation = new PaymentInitiationSctJson();
        AccountReference debtorAccountReference = new AccountReference();
        debtorAccountReference.setIban(paymentBodyObj.getDebtorBankAccount().getIban());

        AccountReference creditorAccountReference = new AccountReference();
        creditorAccountReference.setIban(paymentBodyObj.getReceiverIban());

        Amount amount = new Amount();
        amount.setAmount(paymentBodyObj.getAmount().toString());
        //todo: @age currency is missing in SinglePayment
        amount.setCurrency("EUR");

        paymentInitiation.setDebtorAccount(debtorAccountReference);
        paymentInitiation.setCreditorAccount(creditorAccountReference);
        paymentInitiation.setInstructedAmount(amount);
        paymentInitiation.setCreditorName(paymentBodyObj.getReceiver());
        paymentInitiation.setRemittanceInformationUnstructured(paymentBodyObj.getPurpose());
        return paymentInitiation;
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

    private AuthorisationCodeResponse requestAuthorizationCodeForPayment(String bankingUrl, TransactionRequest paymentRequest, ApiClient apiClient) {
        PaymentInitiationServicePisApi service = createPaymentInitiationServicePisApi(apiClient);

        String paymentId = paymentRequest.getTransaction().getPaymentId();
        String authorisationId = paymentRequest.getAuthorisationId();

        UUID xRequestId = UUID.randomUUID();
        String psuId = paymentRequest.getBankAccess().getBankLogin();
        String corporateId = paymentRequest.getBankAccess().getBankLogin2();
        SelectPsuAuthenticationMethod body = buildSelectPsuAuthenticationMethod(paymentRequest.getTanTransportType().getId());
        Xs2aTanSubmit tanSubmit = new Xs2aTanSubmit(bankingUrl, paymentId, authorisationId, psuId, corporateId);

        try {
            Map<String, Object> updatePsuData = (Map<String, Object>) service.updatePaymentPsuData(SINGLE_PAYMENT_SERVICE, SEPA_CREDIT_TRANSFERS,
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

    private AuthorisationCodeResponse buildAuthorisationCodeResponse(Map<String, Object> updatePsuData, Xs2aTanSubmit tanSubmit) {
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

        return CreateConsentResponse.builder()
                       .consentId(consentId)
                       .validUntil(consentInformation.getValidUntil())
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

    private List<AccountReference> toAccountReferences(List<domain.AccountReference> accounts) {
        ArrayList<AccountReference> accountReferences = new ArrayList<>();
        for (domain.AccountReference account : accounts) {
            AccountReference accountReference = new AccountReference();
            accountReference.setIban(account.getIban());
            accountReference.setCurrency(account.getCurrency());
            accountReferences.add(accountReference);
        }
        return accountReferences;
    }

    private ConsentsResponse201 createConsent(String bankingUrl, BankAccess bankAccess, String pin,
                                              Consents consents) throws ApiException {
        UUID session = UUID.randomUUID();
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        ConsentsResponse201 consent = ais.createConsent(
                session, consents, null, null, null, bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(),
                null, "false", null, null, null, PSU_IP_ADDRESS,
                null,
                null, null, null, null, null, null, null, null
        );

        StartScaprocessResponse startScaprocessResponse = ais.startConsentAuthorisation(consent.getConsentId(),
                                                                                        session, null, null, null,
                                                                                        bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, null, null,
                                                                                        null, null, null, null, null,
                                                                                        null, null, null);

        String authorisationLink =
                startScaprocessResponse.getLinks().get("startAuthorisationWithPsuAuthentication").toString();
        String authorizationId = StringUtils.substringAfterLast(authorisationLink, "/");

        UpdatePsuAuthentication updatePsuAuthentication = buildUpdatePsuAuthorisationBody(pin);

        Map<String, Object> updatePsuResponse =
                (Map<String, Object>) ais.updateConsentsPsuData(consent.getConsentId(), authorizationId, session,
                                                                updatePsuAuthentication, null, null,
                                                                null, bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, null,
                                                                null, null, null, null, null, null,
                                                                null, null, null);

        List<Map> scaMethods = (List<Map>) updatePsuResponse.get(SCA_METHODS);
        String otp = (String) scaMethods
                                      .stream()
                                      .map(x -> x.get("authenticationMethodId"))
                                      .filter(x -> "901".equals(x)).findFirst().get(); //TODO hardcoded SMS

        SelectPsuAuthenticationMethod selectPsuAuthenticationMethod = new SelectPsuAuthenticationMethod();
        selectPsuAuthenticationMethod.setAuthenticationMethodId(otp);

        updatePsuResponse = (Map<String, Object>) ais.updateConsentsPsuData(
                consent.getConsentId(), authorizationId, session, selectPsuAuthenticationMethod, null, null, null,
                bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, PSU_IP_ADDRESS, null, null, null,
                null, null, null, null, null,
                null
        );

        TransactionAuthorisation transactionAuthorisation = new TransactionAuthorisation();
        transactionAuthorisation.setScaAuthenticationData("dontcare"); // TODO we need this step but there is no
        // actual TAN verification

        updatePsuResponse = (Map<String, Object>) ais.updateConsentsPsuData(
                consent.getConsentId(), authorizationId, session, transactionAuthorisation, null, null, null,
                bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, PSU_IP_ADDRESS, null, null, null,
                null, null, null, null, null,
                null
        );

        return consent;
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
        apiClient.setHttpClient(client);
        Optional.ofNullable(bankingUrl)
                .ifPresent(url -> apiClient.setBasePath(url));

        return apiClient;
    }

    ApiClient createApiClient(String bankingUrl) {
        return createApiClient(bankingUrl, null);
    }

}
