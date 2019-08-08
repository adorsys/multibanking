package de.adorsys.multibanking.service;

import de.adorsys.multibanking.bg.BankingGatewayAdapter;
import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.smartanalytics.core.RulesService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class XS2ATest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private UserRepositoryIf userRepository;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private AnalyticsRepositoryIf analyticsRepository;
    @Autowired
    private RulesService rulesService;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @Value("${RULES_URL:file:/Users/alexg/Downloads/rules_base_v4.2.csv}")
    private File rulesFile;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new BankingGatewayAdapter());
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new BankingGatewayAdapter());
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new BankingGatewayAdapter());

        if (rulesFile.exists()) {
            rulesService.newRules(rulesFile.getName(), new FileInputStream(rulesFile));
        }

        bankRepository.findByBankCode(System.getProperty("blz")).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", System.getProperty("blz"), BankApi.HBCI);
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    @Test
    public void testSyncBookings() throws Exception {
        UserEntity userEntity = TestUtil.getUserEntity("test-user-id");
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id",
            System.getProperty("blz"), System.getProperty("pin"));
        bankAccessEntity.setBankLogin(System.getProperty("login"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(true);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            BankApi.HBCI);

        BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
            .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals(System.getProperty("account")))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(BankAccountEntity.class, System.getProperty("account")));

        bankAccountEntitity.setId("test-account-id");

        bookingService.syncBookings(bankAccessEntity, bankAccountEntitity, BankApi.HBCI);

        AccountAnalyticsEntity analyticsEntity = analyticsRepository.findLastByUserIdAndAccountId("test-user-id",
            "test-account-id")
            .orElseThrow(() -> new ResourceNotFoundException(AccountAnalyticsEntity.class, "test-account-id"));

        log.info(analyticsEntity.toString());
    }
}
