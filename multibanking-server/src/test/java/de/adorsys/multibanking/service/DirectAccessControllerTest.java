package de.adorsys.multibanking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.spi.Consent;
import de.adorsys.multibanking.domain.spi.ConsentStatus;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MissingAuthorisationException;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.exception.StrongCustomerAuthorisationException;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.web.DirectAccessController;
import de.adorsys.multibanking.web.model.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static de.adorsys.multibanking.service.TestUtil.createBooking;
import static de.adorsys.multibanking.web.model.ScaStatusTO.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.kapott.hbci.manager.HBCIVersion.HBCI_300;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.MockUtil.isMock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, FongoConfig.class, MapperConfig.class}, webEnvironment =
    SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class DirectAccessControllerTest {

    @Value("${bankinggateway.auth.url}")
    private String consentAuthUrl;

    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;
    @MockBean
    private BankingGatewayAdapter bankingGatewayAdapter;

    @LocalServerPort
    private int port;

    private Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createConsent_should_return_a_consent_with_method_list_for_HBCI() throws Exception {
        BankAccessTO access = createBankAccess();
        setupHBCI(access.getIban());
        RequestSpecification request = createConsentRequest(access);

        Response response = request.post("http://localhost:" + port + "/api/v1/direct/consents");

        assertConsent(response, HttpStatus.CREATED, ScaStatusTO.PSU_AUTHORISED, 2);
    }

    @Test
    public void createConsent_should_return_a_consent_with_method_list_for_BankingGateway() throws Exception {
        BankAccessTO access = createBankAccess();
        setupBankingGateway(access.getIban());
        RequestSpecification request = createConsentRequest(access);

        Response response = request.post("http://localhost:" + port + "/api/v1/direct/consents");

        assertConsent(response, HttpStatus.CREATED, ScaStatusTO.PSU_AUTHORISED, 2);
    }

    @Test
    public void createConsent_should_return_a_consent_with_a_challenge_for_BankingGateway_with_one_method() throws Exception {
        BankAccessTO access = createBankAccess();
        setupBankingGateway(access.getIban());
        RequestSpecification request = createConsentRequest(access);

        Response response = request.post("http://localhost:" + port + "/api/v1/direct/consents");

        assertConsent(response, HttpStatus.CREATED, ScaStatusTO.SCA_METHOD_SELECTED, 0, true);
    }

    private void setupHBCI(String iban) {
        prepareBank(hbci4JavaBanking, Iban.valueOf(iban).getBankCode());
    }

    private void setupBankingGateway(String iban) {
        prepareBank(bankingGatewayAdapter, Iban.valueOf(iban).getBankCode());
    }

    private RequestSpecification createConsentRequest(BankAccessTO access) {
        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(access);
        return request;
    }

    private void assertConsent(Response response, HttpStatus httpStatus, ScaStatusTO scaStatus, int scaMethodCount) throws Exception {
        assertConsent(response, httpStatus, scaStatus, scaMethodCount, false);
    }

    private void assertConsent(Response response, HttpStatus httpStatus, ScaStatusTO scaStatus, int scaMethodCount, boolean challengeExists) throws Exception {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getStatusCode()).isEqualTo(httpStatus.value());
        ConsentTO consentResponse =
            objectMapper.readValue(response.getBody().print()
                , ConsentTO.class);
        softly.assertThat(consentResponse.getScaStatus()).isEqualTo(scaStatus);
        softly.assertThat(consentResponse.getScaMethodList()).asList().hasSize(scaMethodCount);
        if (challengeExists) {
            softly.assertThat(consentResponse.getChallenge()).isNotNull();
        } else {
            softly.assertThat(consentResponse.getChallenge()).isNull();
        }
        softly.assertAll();
    }

    @Test
    public void loadAccountList_should_result_in_a_challenge_for_HBCI() {

    }

    @Test
    public void loadAccountList_should_result_in_a_challenge_for_BankingGateway_without_a_given_challenge() {

    }

    @Test
    public void loadAccountList_should_result_in_a_challenge_for_BankingGateway_with_a_given_challenge() {

    }

    @Test
    public void authorizeTransaction_should_result_in_a_finalized_consent_for_HBCI() {

    }

    @Test
    public void authorizeTransaction_should_result_in_a_finalized_consent_for_BankingGateway() {

    }

    @Test
    public void loadAccountList_should_result_in_a_list_of_accounts_for_HBCI_with_a_finalized_consent() {

    }

    @Test
    public void loadAccountList_should_result_in_a_list_of_accounts_for_BankingGateway_with_a_finalized_consent() {

    }

    //    @Ignore
    @Test
    public void verifyCreateBankAccessHbci() throws Exception {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(hbci4JavaBanking, Iban.valueOf(bankAccess.getIban()).getBankCode());

        //create bank access
        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(bankAccess);

        Response response = request.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        //load bookings
        DirectAccessController.LoadBankAccountsResponse loadBankAccountsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        request.body(loadBookingsRequest(loadBankAccountsResponse.getBankAccounts().get(0)));

        response = request.put("http://localhost:" + port + "/api/v1/direct/bookings");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        DirectAccessController.LoadBookingsResponse loadBookingsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
        assertThat(loadBookingsResponse.getBalances().getReadyBalance()).isNotNull();
    }

    @Test
    public void verifyApiNoConsent() throws Exception {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapter, Iban.valueOf(bankAccess.getIban()).getBankCode());
        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        when(bankingGatewayAdapter.getStrongCustomerAuthorisation()).thenReturn(authorisationMock);

        doThrow(new MissingAuthorisationException()).when(authorisationMock).containsValidAuthorisation(any());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(bankAccess);

        Response response = request.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        Messages messages = objectMapper.readValue(response.getBody().print(), Messages.class);
        assertThat(messages.getMessages().iterator().next().getKey()).isEqualTo("NO_AUTHORISATION");
    }

    @Test
    public void verifyApiConsentStatusReceived() throws IOException {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapter, Iban.valueOf(bankAccess.getIban()).getBankCode());
        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        when(bankingGatewayAdapter.getStrongCustomerAuthorisation()).thenReturn(authorisationMock);

        when(authorisationMock.createAuthorisation(any())).thenReturn(createConsentResponse(null,
            ConsentStatus.RECEIVED));

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(createConsentTO(bankAccess.getIban()));
        Response response = request.post("http://localhost:" + port + "/api/v1/direct/authorisations");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        ConsentTO consent = objectMapper.readValue(response.getBody().print(), ConsentTO.class);
        assertThat(consent.getConsentId()).isNotBlank();
        assertThat(consent.getConsentAuthorisationId()).isNotBlank();
        assertThat(consent.getScaStatus()).isEqualTo(RECEIVED);

        doThrow(new StrongCustomerAuthorisationException(createConsentResponse(null, ConsentStatus.RECEIVED), null))
            .when(authorisationMock).containsValidAuthorisation(any());

        request.body(bankAccess);
        response = request.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        Messages messages = objectMapper.readValue(response.getBody().print(), Messages.class);
        assertThat(messages.getMessages().iterator().next().getKey()).isEqualTo("AUTHORISE_CONSENT");
    }

    @Test
    public void verifyApiConsentStatusValid() throws IOException {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapter, Iban.valueOf(bankAccess.getIban()).getBankCode());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(bankAccess);

        when(bankingGatewayAdapter.loadBankAccounts(any()))
            .thenReturn(LoadAccountInformationResponse.builder()
                .bankAccounts(Collections.singletonList(new BankAccount()))
                .build()
            );

        Response response = request.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        DirectAccessController.LoadBankAccountsResponse loadBankAccountsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        //load bookings
        when(bankingGatewayAdapter.loadBookings(any()))
            .thenReturn(LoadBookingsResponse.builder()
                .bookings(Collections.singletonList(createBooking()))
                .build()
            );

        request.body(loadBookingsRequest(loadBankAccountsResponse.getBankAccounts().get(0)));

        response = request.put("http://localhost:" + port + "/api/v1/direct/bookings");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        DirectAccessController.LoadBookingsResponse loadBookingsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String bankCode) {
        prepareBank(onlineBankingService, bankCode, System.getProperty("bankUrl"));
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String bankCode, String bankUrl) {
        if (isMock(onlineBankingService)) {
            when(onlineBankingService.bankSupported(any())).thenReturn(true);
        }

        when(bankingServiceProducer.getBankingService(bankCode)).thenReturn(onlineBankingService);
        when(bankingServiceProducer.getBankingService(onlineBankingService.bankApi())).thenReturn(onlineBankingService);
        when(bankingServiceProducer.getBankingService(BankApi.BANKING_GATEWAY)).thenReturn(bankingGatewayAdapter);

        BankEntity test_bank = bankRepository.findByBankCode(bankCode).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", bankCode, onlineBankingService.bankApi());
            bankEntity.setBankingUrl(bankUrl);
            bankRepository.save(bankEntity);
            return bankEntity;
        });

        if (onlineBankingService instanceof Hbci4JavaBanking && HBCIUtils.getBankInfo(bankCode) == null) {
            BankInfo bankInfo = new BankInfo();
            bankInfo.setBlz(test_bank.getBankCode());
            bankInfo.setPinTanAddress(bankUrl);
            bankInfo.setPinTanVersion(HBCI_300);
            HBCIUtils.addBankInfo(bankInfo);
        }
    }

    private DirectAccessController.LoadBookingsRequest loadBookingsRequest(BankAccountTO bankAccount) {
        DirectAccessController.LoadBookingsRequest request = new DirectAccessController.LoadBookingsRequest();
        request.setAccountId(bankAccount.getId());
        request.setAccessId(bankAccount.getBankAccessId());
        request.setPin(System.getProperty("pin", "12456"));
        return request;
    }

    private Consent createConsentResponse(String redirectUrl, ConsentStatus scaStatus) {
        Consent consent = new Consent();
        consent.setAccounts(Collections.singletonList(new AccountReference(System.getProperty("iban"))));
        consent.setBalances(Collections.singletonList(new AccountReference(System.getProperty("iban"))));
        consent.setTransactions(Collections.singletonList(new AccountReference(System.getProperty("iban"))));
        consent.setScaStatus(scaStatus);
        consent.setValidUntil(LocalDate.now().plusDays(1));
        consent.setRecurringIndicator(false);
        consent.setFrequencyPerDay(1);

        consent.setConsentId(UUID.randomUUID().toString());
        consent.setConsentAuthorisationId(UUID.randomUUID().toString());
        consent.setRedirectUrl(redirectUrl);

        consent.setAuthUrl(consentAuthUrl);

        return consent;
    }

    private ConsentTO createConsentTO(String iban) {
        ConsentTO consentTO = new ConsentTO();
        consentTO.setAccounts(Collections.singletonList(new AccountReferenceTO(iban)));
        consentTO.setBalances(Collections.singletonList(new AccountReferenceTO(iban)));
        consentTO.setTransactions(Collections.singletonList(new AccountReferenceTO(iban)));
        consentTO.setPsuAccountIban(iban);
        consentTO.setValidUntil(LocalDate.now().plusDays(1));
        consentTO.setRecurringIndicator(false);
        consentTO.setFrequencyPerDay(1);

        return consentTO;
    }

    private BankAccessTO createBankAccess() {
        BankAccessTO bankAccessTO = new BankAccessTO();
        bankAccessTO.setIban(System.getProperty("iban",
            new Iban.Builder()
                .countryCode(CountryCode.DE)
                .bankCode("25040090")
                .buildRandom().toString()));
        bankAccessTO.setBankLogin(System.getProperty("login", "test-login"));
        bankAccessTO.setBankLogin2(System.getProperty("login2"));
        bankAccessTO.setPin(System.getProperty("pin", "12456"));
        bankAccessTO.setPsd2ConsentId(UUID.randomUUID().toString());

        return bankAccessTO;
    }
}
