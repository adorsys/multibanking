package de.adorsys.multibanking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.multibanking.Application;
import de.adorsys.multibanking.conf.FongoConfig;
import de.adorsys.multibanking.conf.MapperConfig;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.web.DirectAccessController;
import de.adorsys.multibanking.web.model.BankAccessTO;
import de.adorsys.multibanking.web.model.BankAccountTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, FongoConfig.class, MapperConfig.class}, webEnvironment =
    SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class DirectAccessControllerTest {

    @Autowired
    private BankRepositoryIf bankRepository;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() {
        Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(true);

        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(hbci4JavaBanking);

        bankRepository.findByBankCode(System.getProperty("blz")).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", System.getProperty("blz"));
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    @Test
    public void verifyCreateBankAccess() throws Exception {
        //create bank access
        RequestSpecification req = RestAssured.given();
        req.contentType(ContentType.JSON);
        req.body(createBankAccess());

        Response response = req.put("http://localhost:" + port + "/api/v1/direct/accounts");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        //load bookings
        DirectAccessController.LoadBankAccountsResponse loadBankAccountsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBankAccountsResponse.class);

        assertThat(loadBankAccountsResponse.getBankAccounts()).isNotEmpty();

        req.body(loadBookingsRequest(loadBankAccountsResponse.getBankAccounts().get(0)));

        response = req.put("http://localhost:" + port + "/api/v1/direct/bookings");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        DirectAccessController.LoadBookingsResponse loadBookingsResponse =
            objectMapper.readValue(response.getBody().print()
                , DirectAccessController.LoadBookingsResponse.class);

        assertThat(loadBookingsResponse.getBookings()).isNotEmpty();
        assertThat(loadBookingsResponse.getBalances().getReadyBalance()).isNotNull();
    }

    private DirectAccessController.LoadBookingsRequest loadBookingsRequest(BankAccountTO bankAccount) {
        DirectAccessController.LoadBookingsRequest request = new DirectAccessController.LoadBookingsRequest();
        request.setAccountId(bankAccount.getId());
        request.setAccessId(bankAccount.getBankAccessId());
        request.setPin(System.getProperty("pin"));
        return request;
    }

    private BankAccessTO createBankAccess() {
        BankAccessTO bankAccessTO = new BankAccessTO();
        bankAccessTO.setBankCode(System.getProperty("blz"));
        bankAccessTO.setBankLogin(System.getProperty("login"));
        bankAccessTO.setBankLogin2(System.getProperty("login2"));
        bankAccessTO.setPin(System.getProperty("pin"));

        return bankAccessTO;
    }
}
