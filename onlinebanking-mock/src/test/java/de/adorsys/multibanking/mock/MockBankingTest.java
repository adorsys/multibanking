package de.adorsys.multibanking.mock;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import org.junit.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Ignore
public class MockBankingTest {
    private MockBanking mockBanking;
    private String pin = "password";
    private BankAccess bankAccess;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("mockConnectionUrl", "http://localhost:10010");
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> entity = restTemplate.getForEntity("http://localhost:10010/health", Object.class);
            Assume.assumeTrue(entity.getStatusCode().value() == 200);
        } catch (Exception e) {
            Assume.assumeTrue(false);
        }
    }

    @Before
    public void before() {
        bankAccess = new BankAccess();
        bankAccess.setBankLogin("login");
        mockBanking = new MockBanking();
    }

    @Test
    public void testLoadBankAccounts() {
        LoadAccountInformationRequest loadAccountInformationRequest = new LoadAccountInformationRequest();
        loadAccountInformationRequest.setBankAccess(bankAccess);
        loadAccountInformationRequest.setPin(pin);

        LoadAccountInformationResponse loadAccountInformationResponse =
            mockBanking.loadBankAccounts(loadAccountInformationRequest);
        Assert.assertNotNull(loadAccountInformationResponse.getBankAccounts());
        Assert.assertFalse(loadAccountInformationResponse.getBankAccounts().isEmpty());
    }

    @Test
    public void testLoadBookings() {
        LoadAccountInformationRequest loadAccountInformationRequest = new LoadAccountInformationRequest();
        loadAccountInformationRequest.setBankAccess(bankAccess);
        loadAccountInformationRequest.setPin(pin);

        LoadAccountInformationResponse loadAccountInformationResponse =
            mockBanking.loadBankAccounts(loadAccountInformationRequest);

        Assume.assumeNotNull(loadAccountInformationResponse.getBankAccounts());
        Assume.assumeFalse(loadAccountInformationResponse.getBankAccounts().isEmpty());
        BankAccount bankAccount = loadAccountInformationResponse.getBankAccounts().iterator().next();

        LoadBookingsRequest loadBookingsRequest = new LoadBookingsRequest();
        loadBookingsRequest.setBankAccess(bankAccess);
        loadBookingsRequest.setBankAccount(bankAccount);
        loadBookingsRequest.setPin(pin);

        LoadBookingsResponse response = mockBanking.loadBookings(loadBookingsRequest);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBookings());
        Assert.assertFalse(response.getBookings().isEmpty());
    }
}
