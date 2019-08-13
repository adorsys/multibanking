package de.adorsys.multibanking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.ScaMethodsResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import de.adorsys.multibanking.web.DirectAccessController;
import de.adorsys.multibanking.web.model.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_2FA_REQUIRED;
import static de.adorsys.multibanking.service.TestUtil.createBooking;
import static de.adorsys.multibanking.web.model.ScaStatusTO.*;
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

    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;
    @MockBean
    private BankingGatewayAdapter bankingGatewayAdapterMock;
    @SpyBean
    private ConsentRepositoryIf consentRepository;

    @LocalServerPort
    private int port;

    @Value("${bankinggateway.b2c.url}")
    private String bankingGatewayBaseUrl;
    @Value("${bankinggateway.adapter.url}")
    private String bankingGatewayAdapterUrl;

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
    public void createConsent_should_return_a_authorisationStatus_link_hbci() {
        BankAccessTO access = createBankAccess();
        prepareBank(hbci4JavaBanking, access.getIban());

        JsonPath jsonPath = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createConsentTO(access.getIban()))
            .post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("consentId")).isNotBlank();
        assertThat(jsonPath.getString("authorisationId")).isNotBlank();
        assertThat(jsonPath.getString("_links.authorisationStatus")).isNotBlank();
    }

    @Ignore
    @Test
    public void consent_authorisation_bankinggateway() {
        BankAccessTO access = createBankAccess();
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl), access.getIban());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);

        //1. create consent
        request.body(createConsentTO(access.getIban()));
        JsonPath jsonPath = request.post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        String consentId = jsonPath.getString("consentId");

        assertThat(jsonPath.getString("_links.authorisationStatus.href")).isNotBlank();

        //2. get consent authorisation status
        jsonPath = request.get(jsonPath.getString("_links.authorisationStatus.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(ScaStatusTO.STARTED.toString());

        //3. update psu authentication
        UpdatePsuAuthenticationRequestTO updatePsuAuthentication = new UpdatePsuAuthenticationRequestTO();
        updatePsuAuthentication.setPsuId("Alex.Geist");
        updatePsuAuthentication.setPassword("sandbox");

        jsonPath = request.body(updatePsuAuthentication).put(jsonPath.getString("_links.updateAuthentication.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(PSUAUTHENTICATED.toString());

        //4. select authentication method
        SelectPsuAuthenticationMethodRequestTO authenticationMethodRequestTO =
            new SelectPsuAuthenticationMethodRequestTO();
        authenticationMethodRequestTO.setAuthenticationMethodId(jsonPath.getString("scaMethods[0].id"));

        jsonPath = request.body(authenticationMethodRequestTO).put(jsonPath.getString("_links" +
            ".selectAuthenticationMethod.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(SCAMETHODSELECTED.toString());

        //5. send tan
        TransactionAuthorisationRequestTO transactionAuthorisationRequestTO = new TransactionAuthorisationRequestTO();
        transactionAuthorisationRequestTO.setScaAuthenticationData("alex1");

        jsonPath = request.body(transactionAuthorisationRequestTO).put(jsonPath.getString("_links" +
            ".transactionAuthorisation.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(FINALISED.toString());

        //6. load transactions
        access.setConsentId(consentId);

        request.body(access).put("http://localhost:" + port + "/api/v1/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();
    }

    private BankAccessTO consent_authorisation_hbci(Hbci4JavaBanking hbci4JavaBanking, String challengeUrl) {
        BankAccessTO access = createBankAccess();
        prepareBank(hbci4JavaBanking, access.getIban());

        //1. create consent
        JsonPath jsonPath = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(createConsentTO(access.getIban()))
            .post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        String consentId = jsonPath.getString("consentId");
        String authorisationId = jsonPath.getString("authorisationId");

        assertThat(jsonPath.getString("_links.authorisationStatus.href")).isNotBlank();

        //2. get consent authorisation status
        jsonPath = RestAssured.given()
            .contentType(ContentType.JSON).get(jsonPath.getString("_links.authorisationStatus.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(ScaStatusTO.STARTED.toString());

        //3. update psu authentication
        doReturn(ScaMethodsResponse.builder()
            .tanTransportTypes(Arrays.asList(TestUtil.createTanMethod("Method1"), TestUtil.createTanMethod("Method2")))
            .build()).when(hbci4JavaBanking).authenticatePsu(any(), any());

        UpdatePsuAuthenticationRequestTO updatePsuAuthentication = new UpdatePsuAuthenticationRequestTO();
        updatePsuAuthentication.setPsuId("Alex.Geist");
        updatePsuAuthentication.setPassword("sandbox");

        jsonPath = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(updatePsuAuthentication).put(jsonPath.getString("_links.updateAuthentication.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(PSUAUTHENTICATED.toString());
        assertThat(jsonPath.getString("_links.selectAuthenticationMethod.href")).isNullOrEmpty();

        //4. select authentication method - by calling the desired method
        access.setConsentId(consentId);
        access.setAuthorisationId(authorisationId);
        access.setBankLogin(updatePsuAuthentication.getPsuId());
        access.setBankLogin2(updatePsuAuthentication.getPsuCustomerId());
        access.setPin(updatePsuAuthentication.getPassword());
        access.setScaMethodId(jsonPath.getString("scaMethods[0].id"));

        jsonPath = RestAssured.given()
            .contentType(ContentType.JSON).body(access).post(challengeUrl)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(SCAMETHODSELECTED.toString());

        //5. send tan
        TransactionAuthorisationRequestTO transactionAuthorisationRequestTO = new TransactionAuthorisationRequestTO();
        transactionAuthorisationRequestTO.setScaAuthenticationData("alex1");

        jsonPath = RestAssured.given()
            .contentType(ContentType.JSON).body(transactionAuthorisationRequestTO).put(jsonPath.getString("_links" +
                ".transactionAuthorisation.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(FINALISED.toString());
        return access;
    }

    @Test
    public void bankAccountList_with_consent_hbci() {
        Hbci4JavaBanking hbci4JavaBankingMock = spy(hbci4JavaBanking);

        doThrow(new MultibankingException(HBCI_2FA_REQUIRED))
            .doReturn(LoadAccountInformationResponse.builder().build())
            .when(hbci4JavaBankingMock).loadBankAccounts(any());

        String accountChallengeUrl = "http://localhost:" + port + "/api/v1/direct/accounts";
        BankAccessTO access = consent_authorisation_hbci(hbci4JavaBankingMock, accountChallengeUrl);

        RestAssured.given().contentType(ContentType.JSON)
            .body(access).put(accountChallengeUrl)
            .then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void bookingList_with_consent_hbci() {
        Hbci4JavaBanking hbci4JavaBankingMock = spy(hbci4JavaBanking);

        doThrow(new MultibankingException(HBCI_2FA_REQUIRED))
            .doReturn(LoadBookingsResponse.builder()
                .bookings(new ArrayList<>())
                .build())
            .when(hbci4JavaBankingMock).loadBookings(any());

        String bookingChallengeUrl = "http://localhost:" + port + "/api/v1/direct/bookings";
        BankAccessTO access = consent_authorisation_hbci(hbci4JavaBankingMock, bookingChallengeUrl);

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(access).put(bookingChallengeUrl)
            .then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Ignore
    @Test
    public void verifyCreateBankAccessHbci() throws Exception {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(hbci4JavaBanking, bankAccess.getIban());

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
        verifyApiNoConsent(MultibankingError.INVALID_PIN, "NO_AUTHORISATION");
    }

    @Test
    public void verifyApiConsentWithoutSelectedSCA() throws Exception {
        verifyApiNoConsent(MultibankingError.INVALID_SCA_METHOD, "SELECT_CONSENT_AUTHORISATION");
    }

    @Test
    public void verifyApiConsentWithoutAuthorisedSCA() throws Exception {
        verifyApiNoConsent(MultibankingError.HBCI_2FA_REQUIRED, "AUTHORISE_CONSENT");
    }

    private void verifyApiNoConsent(MultibankingError error, String messageKey) throws Exception {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, bankAccess.getIban());
        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        when(bankingGatewayAdapterMock.getStrongCustomerAuthorisation()).thenReturn(authorisationMock);
        doReturn(Optional.of(new ConsentEntity())).when(consentRepository).findById(bankAccess.getConsentId());

        doThrow(new MultibankingException(error)).when(authorisationMock).validateConsent(any(), any(), any(), any());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(bankAccess);

        Response response = request.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        Messages messages = objectMapper.readValue(response.getBody().print(), Messages.class);
        assertThat(messages.getMessages().iterator().next().getKey()).isEqualTo(messageKey);
    }

    @Test
    public void verifyApiConsentStatusValid() throws IOException {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, bankAccess.getIban());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(bankAccess);

        when(bankingGatewayAdapterMock.loadBankAccounts(any()))
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
        when(bankingGatewayAdapterMock.loadBookings(any()))
            .thenReturn(LoadBookingsResponse.builder()
                .bookings(Collections.singletonList(createBooking()))
                .build()
            );

        request.body(bankAccess);

        response = request.put("http://localhost:" + port + "/api/v1/direct/bookings");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        DirectAccessController.LoadBookingsResponse loadBookingsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String iban) {
        prepareBank(onlineBankingService, iban, System.getProperty("bankUrl"));
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String iban, String bankUrl) {
        if (isMock(onlineBankingService)) {
            when(onlineBankingService.bankSupported(any())).thenReturn(true);
        }

        String bankCode = Iban.valueOf(iban).getBankCode();

        when(bankingServiceProducer.getBankingService(bankCode)).thenReturn(onlineBankingService);
        when(bankingServiceProducer.getBankingService(onlineBankingService.bankApi())).thenReturn(onlineBankingService);

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

    private ConsentTO createConsentTO(String iban) {
        ConsentTO consentTO = new ConsentTO();
        consentTO.setAccounts(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setBalances(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setTransactions(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setPsuAccountIban(iban);
        consentTO.setValidUntil(LocalDate.now().plusDays(1));
        consentTO.setRecurringIndicator(false);
        consentTO.setFrequencyPerDay(1);

        return consentTO;
    }

    private BankAccessTO createBankAccess() {
        BankAccessTO bankAccessTO = new BankAccessTO();
        bankAccessTO.setIban(System.getProperty("iban", "DE34900000019090909000"));
        bankAccessTO.setBankLogin(System.getProperty("login", "test-login"));
        bankAccessTO.setBankLogin2(System.getProperty("login2"));
        bankAccessTO.setPin(System.getProperty("pin", "12456"));
        bankAccessTO.setConsentId(UUID.randomUUID().toString());

        return bankAccessTO;
    }
}
