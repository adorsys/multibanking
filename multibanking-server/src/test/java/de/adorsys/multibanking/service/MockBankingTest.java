package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ExternalAuthorisationRequiredException;
import de.adorsys.multibanking.mock.MockBanking;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.smartanalytics.core.RulesService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import java.security.Security;
import java.util.Optional;

import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class MockBankingTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private AnalyticsRepositoryIf analyticsRepository;
    @Autowired
    private UserRepositoryIf userRepository;
    @Autowired
    private RulesService rulesService;

    @Value("${RULES_URL:file:/Users/alexg/Downloads/rules.csv}")
    private File rulesFile;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();

        System.setProperty("FIGO_CLIENT_ID", "CdunSr9hi4Q6rL65u-l-coQngofLdNbyjACwFoDOd_OU");
        System.setProperty("FIGO_SECRET", "Sx9FNf1Uze0NZTgXq40ljDeWIpauTJaiZPkhDrc6Vavs");
        System.setProperty("FIGO_TECH_USER", "figo-user");
        System.setProperty("FIGO_TECH_USER_CREDENTIAL", "test123");
        System.setProperty("mockConnectionUrl", "https://flip-multibanking-mock-dev.cloud.adorsys.de");

        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void beforeTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new MockBanking());
        when(bankingServiceProducer.getBankingService(BankApi.MOCK)).thenReturn(new MockBanking());

        rulesService.newRules(rulesFile.getName(), new FileInputStream(rulesFile));
    }

    @Test
    public void testSyncBookings() {
        UserEntity userEntity = TestUtil.getUserEntity("test-user-id");
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id", "19999999"
            , "12345");
        bankAccessEntity.setBankLogin("m.becker");
        bankAccessEntity.setCategorizeBookings(true);
        bankAccessEntity.setStoreAnalytics(true);

        BankAccountEntity bankAccountEntity = TestUtil.getBankAccountEntity("test-account-id");
        bankAccountEntity.setUserId("test-user-id");
        bankAccountEntity.setIban("DE81199999993528307800");
        bankAccountEntity.setAccountNumber("765551851");

        try {
            bookingService.syncBookings(bankAccessEntity, bankAccountEntity, BankApi.MOCK, "12345");
        } catch (ExternalAuthorisationRequiredException e) {
            fail();
        }

        Optional<AccountAnalyticsEntity> analyticsEntity = analyticsRepository.findLastByUserIdAndAccountId("test" +
            "-user-id", "test-account-id");

        log.info(analyticsEntity.get().toString());
    }
}
