package de.adorsys.multibanking.service;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
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
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.*;
import static de.adorsys.multibanking.service.TestUtil.createBooking;
import static de.adorsys.multibanking.web.model.ScaStatusTO.*;
import static org.assertj.core.api.Assertions.assertThat;
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

    private RequestSpecification request = RestAssured.given()
        .contentType(ContentType.JSON)
        .filter(new RequestLoggingFilter())
        .filter(new ResponseLoggingFilter())
        .filter(new ErrorLoggingFilter());

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
        prepareBank(new Hbci4JavaBanking(true), access.getIban(), false);

        JsonPath jsonPath = request.body(createConsentTO(access.getIban()))
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("consentId")).isNotBlank();
        assertThat(jsonPath.getString("authorisationId")).isNotBlank();
        assertThat(jsonPath.getString("_links.authorisationStatus")).isNotBlank();
    }

    //    @Ignore
    @Test
    public void consent_authorisation_bankinggateway_redirect() {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl), bankAccess.getIban(),
            true);

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId("Alex.Geist")
            .pin("sandbox")
            .build();

        JsonPath jsonPath = request.body(createConsentTO(bankAccess.getIban()))
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("_links.redirectUrl.href")).isNotBlank();
    }

    //    @Ignore
    @Test
    public void consent_authorisation_bankinggateway() {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl), bankAccess.getIban(),
            false);

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId("aguex13")
            .pin("aguex13")
            .build();

        consent_authorisation(bankAccess, credentials);
    }

    @Test
    public void consent_authorisation_hbci() {
        BankAccessTO access = createBankAccess();
//        Hbci4JavaBanking hbci4JavaBanking = spy(new Hbci4JavaBanking(true));
//        prepareBank(hbci4JavaBanking, access.getIban(), false);
        Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);
        prepareBank(hbci4JavaBanking, access.getIban(), null, false);
//        prepareBank(hbci4JavaBanking, access.getIban(), "https://obs-qa.bv-zahlungssysteme
//        .de/hbciTunnel/hbciTransfer.jsp", false);

        if (isMock(hbci4JavaBanking)) {
            //mock hbci authenticate psu
            doReturn(ScaMethodsResponse.builder()
                .tanTransportTypes(Arrays.asList(TestUtil.createTanMethod("Method1"), TestUtil.createTanMethod(
                    "Method2")))
                .build()).when(hbci4JavaBanking).getStrongCustomerAuthorisation().updatePsuAuthentication(any());

            //mock bookings response
            LoadBookingsResponse scaRequiredResponse = LoadBookingsResponse.builder().build();
            scaRequiredResponse.setAuthorisationCodeResponse(AuthorisationCodeResponse.builder().build());

            doReturn(scaRequiredResponse)
                .doReturn(LoadBookingsResponse.builder()
                    .bookings(new ArrayList<>())
                    .build())
                .when(hbci4JavaBanking).loadBookings(any());
        }

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId(System.getProperty("login", "login"))
            .userId(System.getProperty("login2", null))
            .pin(System.getProperty("pin", "pin"))
            .build();

        consent_authorisation(access, credentials);
    }

    public void consent_authorisation(BankAccessTO bankAccess, CredentialsTO credentialsTO) {

        //1. create consent
        JsonPath jsonPath = request.body(createConsentTO(bankAccess.getIban()))
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        bankAccess.setConsentId(jsonPath.getString("consentId"));

        assertThat(jsonPath.getString("_links.authorisationStatus.href")).isNotBlank();

        //2. get consent authorisation status
        jsonPath = request.get(jsonPath.getString("_links.authorisationStatus.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(RECEIVED.toString(), ScaStatusTO.STARTED.toString());

        //3. update psu authentication
        UpdatePsuAuthenticationRequestTO updatePsuAuthentication = new UpdatePsuAuthenticationRequestTO();
        updatePsuAuthentication.setPsuId(credentialsTO.getCustomerId());
        updatePsuAuthentication.setPsuCorporateId(credentialsTO.getUserId());
        updatePsuAuthentication.setPassword(credentialsTO.getPin());

        jsonPath = request.body(updatePsuAuthentication).put(jsonPath.getString("_links.updateAuthentication.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        String scaStatus = jsonPath.getString("scaStatus");

        //4. select authentication method (optional), can be skipped by banks in case of selection not needed
        if (scaStatus.equals(PSUAUTHENTICATED.toString())) {
            String selectAuthenticationMethodLink = jsonPath.getString("_links" + ".selectAuthenticationMethod.href");
            String sceMethodId = jsonPath.getString("scaMethods[0].id");

            //hbci case
            if (selectAuthenticationMethodLink == null) {
                DirectAccessController.LoadBookingsChallengeRequest loadBookingsChallengeRequest =
                    new DirectAccessController.LoadBookingsChallengeRequest();

                loadBookingsChallengeRequest.setScaMethodId(sceMethodId);
                loadBookingsChallengeRequest.setBankAccess(bankAccess);

                jsonPath = request
                    .body(loadBookingsChallengeRequest)
                    .post(getRemoteMultibankingUrl() + "/api/v1/direct/bookings")
                    .then().assertThat().statusCode(HttpStatus.OK.value())
                    .and().extract().jsonPath();
            } else {
                //xs2a case
                SelectPsuAuthenticationMethodRequestTO authenticationMethodRequestTO =
                    new SelectPsuAuthenticationMethodRequestTO();
                authenticationMethodRequestTO.setAuthenticationMethodId(sceMethodId);

                jsonPath = request.body(authenticationMethodRequestTO).put(selectAuthenticationMethodLink)
                    .then().assertThat().statusCode(HttpStatus.OK.value())
                    .and().extract().jsonPath();
                assertThat(jsonPath.getString("scaStatus")).isEqualTo(SCAMETHODSELECTED.toString());
            }
        }

        if (jsonPath.get("bookings") != null) {
            //response contains bookings -> sca not needed
            return;
        }

        //5. send tan
        TransactionAuthorisationRequestTO transactionAuthorisationRequestTO = new TransactionAuthorisationRequestTO();
        transactionAuthorisationRequestTO.setScaAuthenticationData("alex1");

        jsonPath = request.body(transactionAuthorisationRequestTO).put(jsonPath.getString("_links" +
            ".transactionAuthorisation.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isEqualTo(FINALISED.toString());

        //6. load transactions
        DirectAccessController.LoadBookingsRequest loadBookingsRequest =
            new DirectAccessController.LoadBookingsRequest();
        if (jsonPath.getString("bankAccounts") != null) {
            loadBookingsRequest.setUserId(jsonPath.getString("bankAccounts[0].userId"));
            loadBookingsRequest.setAccessId(jsonPath.getString("bankAccounts[0].bankAccessId"));
            loadBookingsRequest.setAccountId(jsonPath.getString("bankAccounts[0].id"));
        }

        loadBookingsRequest.setBankAccess(bankAccess);

        request.body(loadBookingsRequest).put(getRemoteMultibankingUrl() + "/api/v1/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();
    }

    @Test
    public void verifyApi() {
        verifyApi(INVALID_PIN, "NO_AUTHORISATION");
    }

    @Test
    public void verifyApiConsentWithoutSelectedSCA() {
        verifyApi(INVALID_SCA_METHOD, "SELECT_CONSENT_AUTHORISATION");
    }

    @Test
    public void verifyApiConsentWithoutAuthorisedSCA() {
        verifyApi(INVALID_CONSENT_STATUS, "AUTHORISE_CONSENT");
    }

    private void verifyApi(MultibankingError error, String messageKey) {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, bankAccess.getIban(), false);

        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        when(bankingGatewayAdapterMock.getStrongCustomerAuthorisation()).thenReturn(authorisationMock);
        doReturn(Optional.of(new ConsentEntity())).when(consentRepository).findById(bankAccess.getConsentId());
        doThrow(new MultibankingException(error)).when(authorisationMock).validateConsent(any(), any(), any(), any());

        DirectAccessController.LoadAccountsRequest loadAccountsRequest =
            new DirectAccessController.LoadAccountsRequest();
        loadAccountsRequest.setBankAccess(bankAccess);

        Messages messages = request.body(loadAccountsRequest)
            .put(getRemoteMultibankingUrl() + "/api/v1/direct/accounts")
            .then().assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().body().as(Messages.class);

        assertThat(messages.getMessages().iterator().next().getKey()).isEqualTo(messageKey);
    }

    @Test
    public void verifyApiConsentStatusValid() {
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, bankAccess.getIban(), false);

        when(bankingGatewayAdapterMock.loadBankAccounts(any()))
            .thenReturn(LoadAccountInformationResponse.builder()
                .bankAccounts(Collections.singletonList(new BankAccount()))
                .build()
            );

        DirectAccessController.LoadAccountsRequest loadAccountsRequest =
            new DirectAccessController.LoadAccountsRequest();
        loadAccountsRequest.setBankAccess(bankAccess);

        DirectAccessController.LoadBankAccountsResponse loadBankAccountsResponse = request
            .body(loadAccountsRequest)
            .put(getRemoteMultibankingUrl() + "/api/v1/direct/accounts")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessController.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        //load bookings
        when(bankingGatewayAdapterMock.loadBookings(any()))
            .thenReturn(LoadBookingsResponse.builder()
                .bookings(Collections.singletonList(createBooking()))
                .build()
            );

        DirectAccessController.LoadBookingsRequest loadBookingsRequest =
            new DirectAccessController.LoadBookingsRequest();
        loadBookingsRequest.setUserId(loadBankAccountsResponse.getBankAccounts().get(0).getUserId());
        loadBookingsRequest.setAccessId(loadBankAccountsResponse.getBankAccounts().get(0).getBankAccessId());
        loadBookingsRequest.setAccountId(loadBankAccountsResponse.getBankAccounts().get(0).getId());

        DirectAccessController.LoadBookingsResponse loadBookingsResponse = request
            .body(loadBookingsRequest)
            .put(getRemoteMultibankingUrl() + "/api/v1/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessController.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String iban, boolean redirectPreferred) {
        prepareBank(onlineBankingService, iban, System.getProperty("bankUrl"), redirectPreferred);
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String iban, String bankUrl,
                             boolean redirectPreferred) {
        if (isMock(onlineBankingService)) {
            when(onlineBankingService.bankSupported(any())).thenReturn(true);
        }

        String bankCode = Iban.valueOf(iban).getBankCode();

        when(bankingServiceProducer.getBankingService(bankCode)).thenReturn(onlineBankingService);
        when(bankingServiceProducer.getBankingService(onlineBankingService.bankApi())).thenReturn(onlineBankingService);

        BankEntity test_bank = bankRepository.findByBankCode(bankCode).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", bankCode, onlineBankingService.bankApi());
            bankEntity.setName("UNITTEST BANK");
            bankEntity.setBankingUrl(bankUrl);
            bankEntity.setRedirectPreferred(redirectPreferred);
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

    private ConsentTO createConsentTO(String iban) {
        ConsentTO consentTO = new ConsentTO();
        consentTO.setAccounts(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setBalances(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setTransactions(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setPsuAccountIban(iban);
        consentTO.setValidUntil(LocalDate.now().plusDays(1));
        consentTO.setRecurringIndicator(false);
        consentTO.setFrequencyPerDay(1);
        consentTO.setTppRedirectUri("https://www.google.com");

        return consentTO;
    }

    private BankAccessTO createBankAccess() {
        BankAccessTO bankAccessTO = new BankAccessTO();
        bankAccessTO.setIban(System.getProperty("iban", "DE34900000019090909000"));
        return bankAccessTO;
    }

    private String getRemoteMultibankingUrl() {
        return "http://localhost:" + port;
//        return "http://localhost:8081";
//        return "https://dev-bankinggateway-multibanking-multibankingservice.cloud.adorsys.de";
    }
}
