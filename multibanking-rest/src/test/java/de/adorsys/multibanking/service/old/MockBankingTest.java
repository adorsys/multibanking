package de.adorsys.multibanking.service.old;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.UserDataService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.onlinebanking.mock.MockBanking;
import domain.BankApi;

/**
 * Created by alexg on 09.10.17.
 * @author fpo 2018-03-24 02:26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"InMemory"})
@Ignore
public class MockBankingTest {

    @Autowired
    private BookingService bookingService;
//    @Autowired
//    private AnalyticsService2 analyticsService;
    @Autowired
    private UserDataService uds;
    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @BeforeClass
    public static void beforeClass() {
		TestConstants.setup();

        System.setProperty("FIGO_CLIENT_ID", "CdunSr9hi4Q6rL65u-l-coQngofLdNbyjACwFoDOd_OU");
        System.setProperty("FIGO_SECRET", "Sx9FNf1Uze0NZTgXq40ljDeWIpauTJaiZPkhDrc6Vavs");
        System.setProperty("FIGO_TECH_USER", "figo-user");
        System.setProperty("FIGO_TECH_USER_CREDENTIAL", "test123");
        System.setProperty("mockConnectionUrl", "https://multibanking-mock.dev.adorsys.de");

        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void beforeTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.MOCK)).thenReturn(new MockBanking());
    }

    @Test
    public void testSyncBookings() {
    	uds.createUser(null);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id", "19999999", "12345");
        bankAccessEntity.setBankLogin("m.becker");
        bankAccessEntity.setCategorizeBookings(true);
        bankAccessEntity.setStoreAnalytics(true);

        BankAccountEntity bankAccountEntity = TestUtil.getBankAccountEntity("test-account-id");
        bankAccountEntity.setUserId("test-user-id");
        bankAccountEntity.setIban("DE81199999993528307800");
        bankAccountEntity.setAccountNumber("765551851");

        bookingService.syncBookings(bankAccessEntity.getId(), bankAccountEntity.getId(), BankApi.MOCK, "12345");
        // TODO load user data and check analytics present.
//        DSDocument loadDomainAnalytics = analyticsService.loadDomainAnalytics("test-access-id", "test-account-id");
        
//        Assert.assertNotNull(loadDomainAnalytics);
    }
}
