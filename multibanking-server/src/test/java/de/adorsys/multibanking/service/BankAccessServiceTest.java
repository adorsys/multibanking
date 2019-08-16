package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.InvalidPinException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.mock.MockBanking;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.smartanalytics.config.EnableSmartanalytics;
import de.adorsys.smartanalytics.config.EnableSmartanalyticsMongoPersistence;
import de.adorsys.smartanalytics.core.AnalyticsConfigProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.util.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableSmartanalytics
@EnableSmartanalyticsMongoPersistence
public class BankAccessServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private UserRepositoryIf userRepository;
    @Autowired
    private BankAccessRepositoryIf bankAccessRepository;
    @Autowired
    private BankAccountRepositoryIf bankAccountRepository;
    @Autowired
    private BankAccessService bankAccessService;
    @Autowired
    private DeleteExpiredUsersScheduled userScheduler;
    @MockBean
    private FigoBanking figoBanking;
    @MockBean
    private MockBanking mockBanking;
    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;
    @MockBean
    private AnalyticsConfigProvider analyticsConfigProvider;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(mockBanking);
        bankRepository.findByBankCode("76090500").orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Sparda Bank", "76090500", BankApi.HBCI);
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    @Test
    public void create_bank_access_not_supported() {
        when(mockBanking.bankSupported(anyString())).thenReturn(false);
        thrown.expect(InvalidBankAccessException.class);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("login", "access", null);
        bankAccessEntity.setBankCode("unsupported");
        bankAccessService.createBankAccess(bankAccessEntity, null);
    }

    @Test
    public void create_bank_access_no_accounts() {
        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any())).thenReturn(LoadAccountInformationResponse.builder().build());
        thrown.expect(InvalidBankAccessException.class);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("login", "access", "76090500");
        bankAccessService.createBankAccess(bankAccessEntity, null);
    }

    @Test
    public void create_bank_access_invalid_pin() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("login", "access", "76090500");

        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any()))
            .thenThrow(new InvalidPinException("access"));
        thrown.expect(InvalidPinException.class);

        bankAccessService.createBankAccess(bankAccessEntity, null);
    }

    @Test
    public void create_bank_access_ok() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("login", "access", "76090500");

        when(mockBanking.bankSupported(anyString())).thenReturn(true);
        when(mockBanking.loadBankAccounts(any()))
            .thenReturn(LoadAccountInformationResponse.builder()
                .bankAccounts(Collections.singletonList(TestUtil.getBankAccountEntity("account")))
                .build());

        bankAccessService.createBankAccess(bankAccessEntity, null);

        notNull(userRepository.findById("login"), "user not exists");
        notNull(bankAccessRepository.findByUserIdAndId("login", "access"), "bankaccess not exists");
        notNull(bankAccountRepository.findByUserIdAndId("login", "account"), "bankaccount not exists");
    }

    @Test
    public void when_delete_bankAcces_user_exist_should_return_false() {
        String userId = UUID.randomUUID().toString();
        userRepository.save(TestUtil.getUserEntity(userId));

        boolean deleteBankAccess = bankAccessService.deleteBankAccess(userId, "access");
        assertThat(deleteBankAccess).isEqualTo(false);
    }

    @Test
    public void when_delete_bankAcces_user_exist_should_return_true() {
        userRepository.save(TestUtil.getUserEntity("login"));
        bankAccessRepository.save(TestUtil.getBankAccessEntity("login", "access", null));

        boolean deleteBankAccess = bankAccessService.deleteBankAccess("login", "access");
        assertThat(deleteBankAccess).isEqualTo(true);
    }

    @Test
    public void get_bank_code() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("login", "access",
            "code");

        bankAccessRepository.save(bankAccessEntity);

        String bankCode = bankAccessRepository.getBankCode(bankAccessEntity.getId());
        assertThat(bankCode).isEqualTo("code");
    }

    @Test
    public void cleaup_users_job() {
        UserEntity userEntity1 = TestUtil.getUserEntity("testUser1");
        userEntity1.setExpireUser(LocalDateTime.now());

        userRepository.save(userEntity1);

        UserEntity userEntity2 = TestUtil.getUserEntity("testUser2");
        userEntity1.setExpireUser(LocalDateTime.now().plusMinutes(1));

        userRepository.save(userEntity2);

        userScheduler.deleteJob();

        Optional<UserEntity> testUser1 = userRepository.findById("testUser1");
        Optional<UserEntity> testUser2 = userRepository.findById("testUser2");
        assertThat(testUser1.isPresent()).isFalse();
        assertThat(testUser2.isPresent()).isTrue();
    }

    @Test
    public void searchBank() {
        notEmpty(bankRepository.search("76090"), "bank not found");
        isTrue(bankRepository.search("76090500").size() == 1, "wrong search result");
        isTrue(bankRepository.search("XYZ").size() == 0, "wrong search result");
        notEmpty(bankRepository.search("Sparda"), "bank not found");
        notEmpty(bankRepository.search("Sparda Bank"), "bank not found");
    }

}
