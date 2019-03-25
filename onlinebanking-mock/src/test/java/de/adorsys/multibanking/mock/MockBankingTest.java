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
    MockBanking mockBanking;
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
        LoadAccountInformationResponse loadAccountInformationResponse = mockBanking.loadBankAccounts(null,
                LoadAccountInformationRequest.builder()
                        .bankAccess(bankAccess)
                        .pin(pin)
                        .build());
        Assert.assertNotNull(loadAccountInformationResponse.getBankAccounts());
        Assert.assertFalse(loadAccountInformationResponse.getBankAccounts().isEmpty());
    }

    @Test
    public void testLoadBookings() {
        LoadAccountInformationResponse loadAccountInformationResponse = mockBanking.loadBankAccounts(null,
                LoadAccountInformationRequest.builder()
                        .bankAccess(bankAccess)
                        .pin(pin)
                        .build());
        Assume.assumeNotNull(loadAccountInformationResponse.getBankAccounts());
        Assume.assumeFalse(loadAccountInformationResponse.getBankAccounts().isEmpty());
        BankAccount bankAccount = loadAccountInformationResponse.getBankAccounts().iterator().next();
        LoadBookingsResponse response = mockBanking.loadBookings(null, LoadBookingsRequest.builder()
                .bankAccess(bankAccess)
                .bankAccount(bankAccount)
                .pin(pin)
                .build());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBookings());
        Assert.assertFalse(response.getBookings().isEmpty());
    }
}
