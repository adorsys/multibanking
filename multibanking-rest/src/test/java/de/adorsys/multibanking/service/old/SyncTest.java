package de.adorsys.multibanking.service.old;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import domain.BankApi;
import hbci4java.Hbci4JavaBanking;

/**
 * Created by alexg on 09.10.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"InMemory"})
@Ignore
public class SyncTest {

    @Autowired
    BookingService bookingService;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    private static final Logger LOG = LoggerFactory.getLogger(SyncTest.class);

    @BeforeClass
    public static void beforeClass() {
		TestConstants.setup();
    }

    @Before
    public void beforeTest() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new Hbci4JavaBanking());
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new Hbci4JavaBanking());
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new Hbci4JavaBanking());
    }

    @Test
    public void testSyncBookings() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id", System.getProperty("blz"), System.getProperty("pin"));
        bankAccessEntity.setBankLogin(System.getProperty("login"));
        bankAccessEntity.setCategorizeBookings(true);
        bankAccessEntity.setStoreAnalytics(true);
        bankAccessEntity.setStoreAnonymizedBookings(true);

        BankAccountEntity bankAccountEntity = TestUtil.getBankAccountEntity("test-account-id");
        bankAccountEntity.setUserId("test-user-id");
        bankAccountEntity.setAccountNumber(System.getProperty("account"));

        bookingService.syncBookings(bankAccessEntity.getId(), bankAccountEntity.getId(), BankApi.HBCI, System.getProperty("pin"));

        // TODO load user data and check analytics present.
//        DSDocument loadDomainAnalytics = analyticsService.loadDomainAnalytics("test-access-id", "test-account-id");
        
//        Assert.assertNotNull(loadDomainAnalytics);
//        LOG.info(analyticsEntity.get().toString());
    }
}
