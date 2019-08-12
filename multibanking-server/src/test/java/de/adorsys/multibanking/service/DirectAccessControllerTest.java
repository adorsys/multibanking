package de.adorsys.multibanking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.ChallengeData;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.ScaMethodsResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.exception.MissingConsentAuthorisationException;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;
import org.mockito.Mockito;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

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

    @LocalServerPort
    private int port;

    @Value("${bankinggateway.base.url:http://localhost:8084}")
    private String bankingGatewayBaseUrl;

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

        RequestSpecification request = createRestRequest(createConsentTO(access.getIban()));
        JsonPath jsonPath = request.post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("consentId")).isNotBlank();
        assertThat(jsonPath.getString("authorisationId")).isNotBlank();
        assertThat(jsonPath.getString("_links.authorisationStatus")).isNotBlank();
    }

    @Test
    public void consent_authorisation_bankinggateway() {
        BankAccessTO access = createBankAccess();
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl), access.getIban());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);

        //1. create consent
        request.body(createConsentTO(access.getIban()));
        JsonPath jsonPath = request.post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

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
    }

    private RequestSpecification createRestRequest(Object body) {
        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);
        request.body(body);
        return request;
    }

    @Test
    public void consent_authorisation_hbci() {
        BankAccessTO access = createBankAccess();
        Hbci4JavaBanking mockBanking = spy(hbci4JavaBanking);
        ScaMethodsResponse hbciResponse = ScaMethodsResponse.builder()
            .tanTransportTypes(Arrays.asList(TestUtil.createTanMethod("Method1"), TestUtil.createTanMethod("Method2")))
            .build();

        doReturn(hbciResponse).when(mockBanking).authenticatePsu(Mockito.<String>any(), any());
        UpdateAuthResponse challengeResponse = new UpdateAuthResponse();
        challengeResponse.setChallenge(new ChallengeData());
        LoadAccountInformationResponse loadAccountInformationResponse = LoadAccountInformationResponse.builder()
            .build();

        prepareBank(mockBanking, access.getIban());

        RequestSpecification request = RestAssured.given();
        request.contentType(ContentType.JSON);

        //1. create consent
        request.body(createConsentTO(access.getIban()));
        JsonPath jsonPath = request.post("http://localhost:" + port + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        String consentId = jsonPath.getString("consentId");
        String authorisationId = jsonPath.getString("authorisationId");
        doThrow(new MissingConsentAuthorisationException(challengeResponse, consentId, authorisationId))
            .doReturn(loadAccountInformationResponse)
            .when(mockBanking).loadBankAccounts(any());

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
        assertThat(jsonPath.getString("_links.selectAuthenticationMethod.href")).isNullOrEmpty();

        //4. select authentication method - by calling the desired method
        BankAccessTO authenticationMethodRequestTO = access;
        authenticationMethodRequestTO.setConsentId(consentId);
        authenticationMethodRequestTO.setAuthorisationId(authorisationId);
        authenticationMethodRequestTO.setBankLogin(updatePsuAuthentication.getPsuId());
        authenticationMethodRequestTO.setBankLogin2(updatePsuAuthentication.getPsuCustomerId());
        authenticationMethodRequestTO.setPin(updatePsuAuthentication.getPassword());
        authenticationMethodRequestTO.setScaMethodId(jsonPath.getString("scaMethods[0].id"));

        String accountChallengeUrl = "http://localhost:" + port + "/api/v1/direct/accounts";

        jsonPath = request.body(authenticationMethodRequestTO).post(accountChallengeUrl)
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

        assertThat(jsonPath .getString("scaStatus")).isEqualTo(FINALISED.toString());

        //6. call method
        BankAccessTO bankAccountListRequest = access;

        String accountUrl = accountChallengeUrl;
        jsonPath = request.body(bankAccountListRequest).put(accountUrl)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(FINALISED.toString());
    }

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
        verifyApiNoConsent(MultibankingError.INVALID_TAN, "AUTHORISE_CONSENT");
    }

    private void verifyApiNoConsent(MultibankingError error, String messageKey) throws Exception {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, bankAccess.getIban());
        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        when(bankingGatewayAdapterMock.getStrongCustomerAuthorisation()).thenReturn(authorisationMock);

        doThrow(new MultibankingException(error)).when(authorisationMock).validateConsent(any(), any(), any());

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

        request.body(loadBookingsRequest(loadBankAccountsResponse.getBankAccounts().get(0)));

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

    private CreateConsentResponse createConsentResponse(String redirectUrl) {
        CreateConsentResponse consentResponse = new CreateConsentResponse();
        consentResponse.setConsentId(UUID.randomUUID().toString());
        consentResponse.setRedirectUrl(redirectUrl);

        return consentResponse;
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
