package de.adorsys.xs2a;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.AccountInformationServiceAisApi;
import de.adorsys.psd2.client.model.*;
import domain.*;
import domain.request.*;
import domain.response.LoadAccountInformationResponse;
import domain.response.LoadBookingsResponse;
import org.apache.commons.lang3.StringUtils;
import spi.OnlineBankingService;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class XS2ABanking implements OnlineBankingService {

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
    public BankApiUser registerUser(Optional<String> bankingUrl, BankAccess bankAccess, String pin) {
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
    public void removeUser(Optional<String> bankingUrl, BankApiUser bankApiUser) {
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        String consentId = loadAccountInformationRequest.getBankApiUser().getProperties().get("allAccountsConsentId");
        try {
            AccountList accountList = ais.getAccountList(UUID.randomUUID(), consentId, false,
                    null, null, null,
                    null, "127.0.0.1", null, null,
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
    public void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser) {
    }

    @Override
    public LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        String consentId =
                Optional.ofNullable(loadBookingsRequest.getBankApiUser().getProperties().get("consentId-" + loadBookingsRequest.getBankAccount().getIban()))
                        .orElseThrow(() -> new MissingConsentException("missing consent for transactions request"));

        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));
        String resourceId = loadBookingsRequest.getBankAccount().getExternalIdMap().get(BankApi.XS2A);
        try {
            TransactionsResponse200Json transactionList = ais.getTransactionList(
                    resourceId, "booked", UUID.randomUUID(),
                    consentId, null, null, null, null,
                    null, null, null, null, null,
                    "127.0.0.1", null, null, null,
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
    public List<BankAccount> loadBalances(Optional<String> bankingUrl, LoadBalanceRequest loadBalanceRequest) {
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
    public Object createPayment(Optional<String> bankingUrl, PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public Object deletePayment(Optional<String> bankingUrl, PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public String submitPayment(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public String submitDelete(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference) {
        return !Optional.ofNullable(bankApiUser.getProperties().get("consentId-" + accountReference)).isPresent();
    }

    @Override
    public void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest) {
        String newConsent = createDedicatedAccountConsent(bankingUrl, startScaRequest, startScaRequest.getBankAccess());
        startScaRequest.getBankApiUser().getProperties().put("consentId-" + startScaRequest.getIban(), newConsent);
    }

    private String createDedicatedAccountConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest,
                                                 BankAccess bankAccess) {
        Consents consents = new Consents();
        consents.setValidUntil(LocalDate.now().plusYears(1));
        consents.setFrequencyPerDay(100);

        AccountReferenceIban accountReferenceIban = new AccountReferenceIban();
        accountReferenceIban.setIban(startScaRequest.getIban());
        accountReferenceIban.setCurrency("EUR");
        List<Object> accounts = Arrays.asList(accountReferenceIban);

        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setTransactions(accounts);
        accountAccess.setAccounts(accounts);
        accountAccess.setBalances(accounts);
        consents.setAccess(accountAccess);

        consents.setRecurringIndicator(true);

        try {
            return createConsent(bankingUrl, bankAccess, startScaRequest.getPin(), consents).getConsentId();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private ConsentsResponse201 createConsent(Optional<String> bankingUrl, BankAccess bankAccess, String pin,
                                              Consents consents) throws ApiException {
        UUID session = UUID.randomUUID();
        AccountInformationServiceAisApi ais = new AccountInformationServiceAisApi(createApiClient(bankingUrl));

        ConsentsResponse201 consent = ais.createConsent(
                session, consents, null, null, null, bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(),
                null, false, null, null, null, "127.0.0.1",
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

        UpdatePsuAuthentication updatePsuAuthentication = new UpdatePsuAuthentication();
        updatePsuAuthentication.psuData(new PsuData()
                .password(pin));

        Map<String, Object> updatePsuResponse =
                (Map<String, Object>) ais.updateConsentsPsuData(consent.getConsentId(), authorizationId, session,
                        updatePsuAuthentication, null, null,
                        null, bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, null,
                        null, null, null, null, null, null,
                        null, null, null);

        List<Map> scaMethods = (List<Map>) updatePsuResponse.get("scaMethods");
        String otp = (String) scaMethods
                .stream()
                .map(x -> x.get("authenticationMethodId"))
                .filter(x -> "901".equals(x)).findFirst().get(); //TODO hardcoded SMS

        SelectPsuAuthenticationMethod selectPsuAuthenticationMethod = new SelectPsuAuthenticationMethod();
        selectPsuAuthenticationMethod.setAuthenticationMethodId(otp);

        updatePsuResponse = (Map<String, Object>) ais.updateConsentsPsuData(
                consent.getConsentId(), authorizationId, session, selectPsuAuthenticationMethod, null, null, null,
                bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, "127.0.0.1", null, null, null,
                null, null, null, null, null,
                null
        );

        TransactionAuthorisation transactionAuthorisation = new TransactionAuthorisation();
        transactionAuthorisation.setScaAuthenticationData("dontcare"); // TODO we need this step but there is no
        // actual TAN verification

        updatePsuResponse = (Map<String, Object>) ais.updateConsentsPsuData(
                consent.getConsentId(), authorizationId, session, transactionAuthorisation, null, null, null,
                bankAccess.getBankLogin(), null, bankAccess.getBankLogin2(), null, "127.0.0.1", null, null, null,
                null, null, null, null, null,
                null
        );

        return consent;
    }

    private ApiClient createApiClient(Optional<String> bankingUrl) {
        ApiClient apiClient = new ApiClient();
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(
                new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        );
        client.setReadTimeout(600, TimeUnit.SECONDS);
        apiClient.setHttpClient(client);
        bankingUrl.ifPresent(url -> apiClient.setBasePath(url));

        return apiClient;
    }

}
