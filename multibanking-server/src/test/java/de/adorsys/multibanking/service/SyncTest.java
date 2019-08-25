package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.smartanalytics.core.RulesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
import java.util.Optional;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class SyncTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private AnalyticsRepositoryIf analyticsRepository;
    @Autowired
    private RulesService rulesService;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @Value("${RULES_URL:file:/Users/alexg/Downloads/rules_base_v4.2x.csv}")
    private File rulesFile;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() throws IOException {
        String params = "25040090=X-BANK|Köln|HYVEDEM1093|99|https://obs-qa.bv-zahlungssysteme.de|https://obs-qa" +
            ".bv-zahlungssysteme.de/hbciTunnel/hbciTransfer.jsp|300|300|";

        Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(IOUtils.toInputStream(params, "ISO-8859-1"), true);
//        hbci4JavaBanking = new Hbci4JavaBanking(null);

        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(hbci4JavaBanking);

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
    public void testSyncBookings() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id", System.getProperty("blz"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(true);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            BankApi.HBCI);
        BankAccountEntity bankAccountEntity = bankAccountEntities.stream()
            .filter(account -> account.getAccountNumber().equals(System.getProperty("account")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("account not found: " + System.getProperty("account")));

        bankAccountEntity.setId("test-account-id");

        bookingService.syncBookings(FINALISED, bankAccessEntity, bankAccountEntity, BankApi.HBCI);

        Optional<AccountAnalyticsEntity> analyticsEntity = analyticsRepository.findLastByUserIdAndAccountId("test" +
            "-user-id", "test-account-id");
        analyticsEntity.ifPresent(accountAnalyticsEntity -> log.info(accountAnalyticsEntity.toString()));

    }
}
