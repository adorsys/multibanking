package de.adorsys.multibanking.service;

import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.config.MongoMapKeyDotReplacementConfiguration;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.exception.MultibankingError;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.spi.StrongCustomerAuthorisable;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.hbci.HbciBanking;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import de.adorsys.multibanking.ing.IngAdapter;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import de.adorsys.multibanking.web.DirectAccessControllerV2;
import de.adorsys.multibanking.web.model.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import java.time.LocalDate;
import java.util.*;

import static de.adorsys.multibanking.domain.BankApi.HBCI;
import static de.adorsys.multibanking.domain.ScaApproach.EMBEDDED;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_CONSENT_STATUS;
import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_SCA_METHOD;
import static de.adorsys.multibanking.service.TestUtil.createBooking;
import static de.adorsys.multibanking.web.model.ScaApproachTO.OAUTH;
import static de.adorsys.multibanking.web.model.ScaStatusTO.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kapott.hbci.manager.HBCIVersion.HBCI_300;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.MockUtil.isMock;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, FongoConfig.class, MongoMapKeyDotReplacementConfiguration.class, MapperConfig.class}, webEnvironment =
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

    @Value("${ing.url}")
    private String ingBaseUrl;
    @Value("${pkcs12.keyStore.url}")
    private String keyStoreUrl;
    @Value("${pkcs12.keyStore.password}")
    private String keyStorePassword;
    @Value("${ing.qwac.alias}")
    private String ingQwacAlias;
    @Value("${ing.qseal.alias}")
    private String ingQsealAlias;
    @Value("${bankinggateway.b2c.url}")
    private String bankingGatewayBaseUrl;
    @Value("${bankinggateway.adapter.url}")
    private String bankingGatewayAdapterUrl;

    private RequestSpecification request = RestAssured.given()
        .contentType(ContentType.JSON)
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
        ConsentTO consentTO = createConsentTO();
        prepareBank(new HbciBanking(null, 0, 0), consentTO.getPsuAccountIban(), false);

        JsonPath jsonPath = request.body(consentTO)
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("consentId")).isNotBlank();
        assertThat(jsonPath.getString("authorisationId")).isNotBlank();
        assertThat(jsonPath.getString("_links.authorisationStatus")).isNotBlank();
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_bankinggateway_redirect() {
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl),
            createConsentTO().getPsuAccountIban(),
            true);

        doRedirect(null);
    }

    private String doRedirect(String prestepConsentId) {
        ConsentTO consent = createConsentTO();
        consent.setId(prestepConsentId);

        JsonPath jsonPath = request.body(consent)
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        String consentId = jsonPath.getString("consentId");
        String statusLink = jsonPath.getString("_links.authorisationStatus.href");

        System.out.println(jsonPath.getString("_links.redirectUrl.href"));

        //enter breakpoint and use link for authorisation
        jsonPath = request.get(statusLink)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(FINALISED.toString());

        return consentId;
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_bankinggateway_decoupled() {
        ConsentTO consentTO = createConsentTO();
        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl),
            consentTO.getPsuAccountIban(),
            false);

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId("aguex12")
            .pin("aguex12")
            .build();

        BankAccessTO bankAccess = createBankAccess();
        JsonPath jsonPath = consent_authorisation(consentTO, bankAccess, credentials);
        System.out.println(jsonPath.getString("psuMessage"));

        //enter breakpoint and use link for authorisation
        jsonPath = request.get(jsonPath.getString("_links.self.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(FINALISED.toString());
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_ing() {
        ConsentTO consentTO = createConsentTO();

        prepareBank(new IngAdapter(ingBaseUrl, keyStoreUrl, keyStorePassword, ingQwacAlias, ingQsealAlias),
            consentTO.getPsuAccountIban(), false);

        //1. create consent
        JsonPath jsonPath = request.body(consentTO)
            .header("Correlation-ID", "TEST123")
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("_links.redirectUrl")).isNotBlank();

        BankAccessTO bankAccess = createBankAccess();
        bankAccess.setConsentId(jsonPath.getString("consentId"));

        //2. get consent authorisation status
        jsonPath = request.get(jsonPath.getString("_links.authorisationStatus.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaApproach")).isEqualTo(OAUTH.toString());

        //3. load bookings
        DirectAccessControllerV2.LoadBookingsRequest LoadBookingsRequest =
            new DirectAccessControllerV2.LoadBookingsRequest();
        LoadBookingsRequest.setBankAccess(bankAccess);
        LoadBookingsRequest.setAuthorisationCode("8b6cd77a-aa44-4527-ab08-a58d70cca286");

        jsonPath = request
            .body(LoadBookingsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("bookings")).isNotBlank();
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_bankinggateway() {
        ConsentTO consentTO = createConsentTO();

        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl),
            consentTO.getPsuAccountIban(),
            false);

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId(System.getProperty("login", "login"))
            .pin(System.getProperty("password", "login"))
            .build();

        consent_authorisation(consentTO, createBankAccess(), credentials);
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_hbci() {
        HbciBanking hbci4JavaBanking = new HbciBanking(null, 0, 0);

        ConsentTO consentTO = createConsentTO();
        prepareBank(hbci4JavaBanking, consentTO.getPsuAccountIban(), false);

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId(System.getProperty("login", "login"))
            .userId(System.getProperty("login2", null))
            .pin(System.getProperty("pin", "pin"))
            .build();

        consent_authorisation(consentTO, createBankAccess(), credentials);
    }

    @Ignore
    @Test
    public void consent_authorisation_hbci_mock() {
        ConsentTO consentTO = createConsentTO();

        HbciBanking hbci4JavaBanking = spy(new HbciBanking(null, 0, 0));
        prepareBank(hbci4JavaBanking, consentTO.getPsuAccountIban(), false);

        //mock hbci authenticate "authenticatePsu" that's why we need to use an answer to manipulate the consent
        StrongCustomerAuthorisable strongCustomerAuthorisable = spy(hbci4JavaBanking.getStrongCustomerAuthorisation());
        doReturn(strongCustomerAuthorisable).when(hbci4JavaBanking).getStrongCustomerAuthorisation();
        // unfortunately we can't mock the private method ``
        doAnswer(invocationOnMock -> {
            List<TanTransportType> fakeList = Arrays.asList(TestUtil.createTanMethod("Method1"),
                TestUtil.createTanMethod(
                    "Method2"));
            UpdatePsuAuthenticationRequest updatePsuAuthentication = invocationOnMock.getArgument(0);
            HbciConsent hbciConsent = (HbciConsent) updatePsuAuthentication.getBankApiConsentData();
            hbciConsent.setStatus(ScaStatus.PSUAUTHENTICATED);
            hbciConsent.setTanMethodList(fakeList);
            UpdateAuthResponse updateAuthResponse = new UpdateAuthResponse(HBCI, EMBEDDED, ScaStatus.PSUAUTHENTICATED);

            updateAuthResponse.setScaMethods(fakeList);
            return updateAuthResponse;
        }).when(strongCustomerAuthorisable).updatePsuAuthentication(any());

        //mock bookings response
        UpdateAuthResponse updateAuthResponse = new UpdateAuthResponse(HBCI, EMBEDDED, ScaStatus.SCAMETHODSELECTED);
        updateAuthResponse.setChallenge(new ChallengeData());

        AuthorisationCodeResponse authorisationCodeResponse = new AuthorisationCodeResponse(null);
        authorisationCodeResponse.setUpdateAuthResponse(updateAuthResponse);

        TransactionsResponse scaRequiredResponse = TransactionsResponse.builder().build();
        scaRequiredResponse.setAuthorisationCodeResponse(authorisationCodeResponse);

        doReturn(scaRequiredResponse)
            .doReturn(TransactionsResponse.builder()
                .bookings(new ArrayList<>())
                .build())
            .when(hbci4JavaBanking).loadTransactions(any());

        CredentialsTO credentials = CredentialsTO.builder()
            .customerId(System.getProperty("login", "login"))
            .userId(System.getProperty("login2", null))
            .pin(System.getProperty("pin", "pin"))
            .build();

        consent_authorisation(consentTO, createBankAccess(), credentials);
    }

    public JsonPath consent_authorisation(ConsentTO consent, BankAccessTO bankAccess, CredentialsTO credentialsTO) {
        //1. create consent
        JsonPath jsonPath = request.body(consent)
            .header("Correlation-ID", "TEST123")
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        bankAccess.setConsentId(jsonPath.getString("consentId"));

        assertThat(jsonPath.getString("_links.authorisationStatus.href")).isNotBlank();

        String linkAuthorisationStatus = jsonPath.getString("_links.authorisationStatus.href");

        //2. get consent authorisation status
        jsonPath = request.get(linkAuthorisationStatus)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(RECEIVED.toString(), STARTED.toString());

        //3. update psu authentication
        UpdatePsuAuthenticationRequestTO updatePsuAuthentication = new UpdatePsuAuthenticationRequestTO();
        updatePsuAuthentication.setPsuId(credentialsTO.getCustomerId());
        updatePsuAuthentication.setPsuCorporateId(credentialsTO.getUserId());
        updatePsuAuthentication.setPassword(credentialsTO.getPin());

        String linkUpdateAuthentication = jsonPath.getString("_links.updateAuthentication.href");

        jsonPath = request.body(updatePsuAuthentication).put(linkUpdateAuthentication)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(PSUAUTHENTICATED.toString(), SCAMETHODSELECTED.toString());

        // get consent auth status after "lazy" startauth
        jsonPath = request.get(linkAuthorisationStatus)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(RECEIVED.toString(), STARTED.toString(),
            SCAMETHODSELECTED.toString(), PSUAUTHENTICATED.toString());

        if (ScaApproachTO.valueOf(jsonPath.getString("scaApproach")) == ScaApproachTO.DECOUPLED) {
            return jsonPath;
        }

        //4. select authentication method (optional), can be skipped by banks in case of selection not needed
        if (jsonPath.getString("scaStatus").equals(PSUAUTHENTICATED.toString())) {
            String selectAuthenticationMethodLink = jsonPath.getString("_links.selectAuthenticationMethod.href");
            Map<String, String> scaMethodParams = jsonPath.get("scaMethods[0]");

            String scaMethodId = scaMethodParams.get("id");
            if (jsonPath.get("scaMethods.find { it.id == '901' }") != null) {
                scaMethodId = "901";
            }

            SelectPsuAuthenticationMethodRequestTO authenticationMethodRequestTO =
                new SelectPsuAuthenticationMethodRequestTO();
            authenticationMethodRequestTO.setAuthenticationMethodId(scaMethodId);
            authenticationMethodRequestTO.setTanMediaName(scaMethodParams.get("medium"));

            jsonPath = request.body(authenticationMethodRequestTO).put(selectAuthenticationMethodLink)
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .and().extract().jsonPath();
            assertThat(jsonPath.getString("scaStatus")).isEqualTo(SCAMETHODSELECTED.toString());
        }

        if (ScaApproachTO.valueOf(jsonPath.getString("scaApproach")) == ScaApproachTO.DECOUPLED) {
            return jsonPath;
        }

        String transactionAuthorisationLink = jsonPath.getString("_links" + ".transactionAuthorisation.href");
        //5. bookings challenge for hbci (Optional) hbci case
        if (transactionAuthorisationLink == null) {
            DirectAccessControllerV2.LoadBookingsRequest loadBookingsRequest = new DirectAccessControllerV2.LoadBookingsRequest();
            loadBookingsRequest.setBankAccess(bankAccess);

            jsonPath = request
                .body(loadBookingsRequest)
                .post(getRemoteMultibankingUrl() + "/api/v2/direct/bookings")
                .then().extract().jsonPath();

            if (jsonPath.get("bookings") != null) {
                //response contains bookings -> sca not needed
                return null;
            }
        }
        //6. send tan
        TransactionAuthorisationRequestTO transactionAuthorisationRequestTO = new TransactionAuthorisationRequestTO();
        transactionAuthorisationRequestTO.setScaAuthenticationData(System.getProperty("tan", "12456"));

        jsonPath = request.body(transactionAuthorisationRequestTO).put(jsonPath.getString("_links.transactionAuthorisation.href"))
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();

        assertThat(jsonPath.getString("scaStatus")).isIn(SCAMETHODSELECTED.toString(), FINALISED.toString());

        //7. load transactions
        DirectAccessControllerV2.LoadBookingsRequest loadBookingsRequest = new DirectAccessControllerV2.LoadBookingsRequest();
        loadBookingsRequest.setBankAccess(bankAccess);
        if (jsonPath.getString("bankAccounts") != null) {
            loadBookingsRequest.setUserId(jsonPath.getString("bankAccounts[0].userId"));
            loadBookingsRequest.setAccessId(jsonPath.getString("bankAccounts[0].bankAccessId"));
            loadBookingsRequest.setAccountId(jsonPath.getString("bankAccounts[0].id"));
        }

        return request.body(loadBookingsRequest).post(getRemoteMultibankingUrl() + "/api/v2/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .and().extract().jsonPath();
    }

    @Test
    public void verifyApiConsentInvalidSCAMethod() {
        verifyApi(INVALID_SCA_METHOD, "SELECT_SCA_METHOD");
    }

    @Test
    public void verifyApiConsentWithoutSelectedSCAMethod() {
        verifyApi(INVALID_CONSENT_STATUS, "SELECT_SCA_METHOD");
    }

    private void verifyApi(MultibankingError throwError, String expectedMessage) {
        ConsentTO consentTO = createConsentTO();
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, consentTO.getPsuAccountIban(), false);

        StrongCustomerAuthorisable authorisationMock = mock(StrongCustomerAuthorisable.class);
        doReturn(authorisationMock).when(bankingGatewayAdapterMock).getStrongCustomerAuthorisation();
        doReturn(new UpdateAuthResponse(HBCI, EMBEDDED, ScaStatus.SCAMETHODSELECTED)).when(authorisationMock).getAuthorisationStatus(bankAccess.getConsentId(), null
            , null);
        doReturn(Optional.of(new ConsentEntity(bankAccess.getConsentId(), null, null, false, null,
            consentTO.getPsuAccountIban(), null))).when(consentRepository).findById(bankAccess.getConsentId());
        doThrow(new MultibankingException(throwError)).when(authorisationMock).validateConsent(any(), any(), any(),
            any());

        DirectAccessControllerV2.LoadAccountsRequest loadAccountsRequest =
            new DirectAccessControllerV2.LoadAccountsRequest();
        loadAccountsRequest.setBankAccess(bankAccess);

        Messages messages = request.body(loadAccountsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/accounts")
            .then().assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().body().as(Messages.class);

        assertThat(messages.getMessages().iterator().next().getKey()).isEqualTo(expectedMessage);
    }

    @Test
    public void verifyApiConsentStatusValid() {
        ConsentTO consentTO = createConsentTO();
        BankAccessTO bankAccess = createBankAccess();
        prepareBank(bankingGatewayAdapterMock, consentTO.getPsuAccountIban(), false);
        fakeConsentValidation(bankingGatewayAdapterMock);

        doReturn(Optional.of(new ConsentEntity(null, null, null, false, null, consentTO.getPsuAccountIban(), null))).when(consentRepository).findById(bankAccess.getConsentId());
        when(bankingGatewayAdapterMock.loadBankAccounts(any()))
            .thenReturn(AccountInformationResponse.builder()
                .bankAccounts(Collections.singletonList(new BankAccount()))
                .build()
            );

        DirectAccessControllerV2.LoadAccountsRequest loadAccountsRequest =
            new DirectAccessControllerV2.LoadAccountsRequest();
        loadAccountsRequest.setBankAccess(bankAccess);

        DirectAccessControllerV2.LoadBankAccountsResponse loadBankAccountsResponse = request
            .body(loadAccountsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/accounts")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessControllerV2.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        //load bookings
        when(bankingGatewayAdapterMock.loadTransactions(any()))
            .thenReturn(TransactionsResponse.builder()
                .bookings(Collections.singletonList(createBooking()))
                .build()
            );

        DirectAccessControllerV2.LoadBookingsRequest loadBookingsRequest =
            new DirectAccessControllerV2.LoadBookingsRequest();
        loadBookingsRequest.setUserId(loadBankAccountsResponse.getBankAccounts().get(0).getUserId());
        loadBookingsRequest.setAccessId(loadBankAccountsResponse.getBankAccounts().get(0).getBankAccessId());
        loadBookingsRequest.setAccountId(loadBankAccountsResponse.getBankAccounts().get(0).getId());

        DirectAccessControllerV2.LoadBookingsResponse loadBookingsResponse = request
            .body(loadBookingsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessControllerV2.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
    }

    @Ignore("uses real data - please setup ENV")
    @Test
    public void consent_authorisation_bankinggateway_oauth() {
        ConsentTO consentTO = createConsentTO();

        prepareBank(new BankingGatewayAdapter(bankingGatewayBaseUrl, bankingGatewayAdapterUrl),
            consentTO.getPsuAccountIban(),
            true);

        //1. initial call
        JsonPath jsonPath = request.body(consentTO)
            .post(getRemoteMultibankingUrl() + "/api/v1/consents")
            .then().assertThat().statusCode(HttpStatus.CREATED.value())
            .and().extract().jsonPath();

        String consentId = jsonPath.getString("consentId");
        String authorisationId = jsonPath.getString("authorisationId");
        String idpUrl = jsonPath.getString("_links.oauthRedirectUrl.href");

        assertThat(idpUrl).isNotBlank();

        log.info("Oauth redirect url: " + idpUrl);
        String authorizationCode = "BREAK_AND_PLACE_AUTHCODE_HERE";

        //2. submit auth code (break to enter auth code)
        TokenRequestTO tokenRequestTO = new TokenRequestTO();
        tokenRequestTO.setAuthorisationCode(authorizationCode);
        request.body(tokenRequestTO)
            .post(getRemoteMultibankingUrl() + "/api/v1/consents/{consentId}/token"
                .replace("{consentId}", consentId))
            .then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());

        //3. perform prestep consent creation (only prestep)
        if (StringUtils.isEmpty(authorisationId)) {
            consentId = doRedirect(consentId); // replace pseudo consent id with final id
        }

        //4. load accounts
        DirectAccessControllerV2.LoadAccountsRequest loadAccountsRequest =
            new DirectAccessControllerV2.LoadAccountsRequest();
        BankAccessTO bankAccess = new BankAccessTO();
        bankAccess.setConsentId(consentId);
        loadAccountsRequest.setBankAccess(bankAccess);

        DirectAccessControllerV2.LoadBankAccountsResponse loadBankAccountsResponse = request
            .body(loadAccountsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/accounts")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessControllerV2.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        //5. load bookings
        DirectAccessControllerV2.LoadBookingsRequest loadBookingsRequest =
            new DirectAccessControllerV2.LoadBookingsRequest();
        loadBookingsRequest.setUserId(loadBankAccountsResponse.getBankAccounts().get(0).getUserId());
        loadBookingsRequest.setAccessId(loadBankAccountsResponse.getBankAccounts().get(0).getBankAccessId());
        loadBookingsRequest.setAccountId(loadBankAccountsResponse.getBankAccounts().get(0).getId());

        DirectAccessControllerV2.LoadBookingsResponse loadBookingsResponse = request
            .body(loadBookingsRequest)
            .post(getRemoteMultibankingUrl() + "/api/v2/direct/bookings")
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(DirectAccessControllerV2.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
    }

    private void fakeConsentValidation(OnlineBankingService onlineBankingService) {
        // mock the sca handler
        when(onlineBankingService.getStrongCustomerAuthorisation()).thenReturn(mock(StrongCustomerAuthorisable.class));
        // return a fake consent
        doReturn(Optional.of(new ConsentEntity())).when(consentRepository).findById(nullable(String.class));
        // sca handler will do nothing with the fake and this result in a positive validation
    }

    private void prepareBank(OnlineBankingService onlineBankingService, String iban,
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
            bankEntity.setRedirectPreferred(redirectPreferred);
            bankEntity.setBic(System.getProperty("bic"));
            bankRepository.save(bankEntity);
            return bankEntity;
        });

        if (onlineBankingService instanceof HbciBanking) {
            BankInfo bankInfo = Optional.ofNullable(HBCIUtils.getBankInfo(bankCode))
                .orElseGet(() -> {
                    BankInfo newBank = new BankInfo();
                    newBank.setBlz(test_bank.getBankCode());
                    newBank.setPinTanVersion(HBCI_300);
                    newBank.setBic(System.getProperty("bic"));
                    HBCIUtils.addBankInfo(newBank);
                    return newBank;
                });

            Optional.ofNullable(System.getProperty("bankUrl"))
                .ifPresent(bankInfo::setPinTanAddress);
        }
    }

    private ConsentTO createConsentTO() {
        // String iban = System.getProperty("iban", "DE60900000020000000001");
        String iban = System.getProperty("iban", "DE16900010021234567890");

        ConsentTO consentTO = new ConsentTO();
        consentTO.setAccounts(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setBalances(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setTransactions(Collections.singletonList(new AccountReferenceTO(iban, null)));
        consentTO.setPsuAccountIban(iban);
        consentTO.setValidUntil(LocalDate.now().plusDays(1));
        consentTO.setRecurringIndicator(false);
        consentTO.setFrequencyPerDay(1);
        consentTO.setTppRedirectUri("https://gurk.adorsys.de");

        return consentTO;
    }

    private BankAccessTO createBankAccess() {
        BankAccessTO bankAccessTO = new BankAccessTO();
        bankAccessTO.setConsentId(UUID.randomUUID().toString());
        return bankAccessTO;
    }

    private String getRemoteMultibankingUrl() {
        return "http://localhost:" + port;
//        return "http://localhost:8081";
//        return "https://dev-bankinggateway-multibanking-multibankingservice.cloud.adorsys.de";
    }
}
