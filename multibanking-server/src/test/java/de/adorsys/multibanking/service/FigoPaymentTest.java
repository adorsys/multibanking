package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.figo.FigoBanking;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static de.adorsys.multibanking.domain.ScaStatus.FINALISED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class FigoPaymentTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserRepositoryIf userRepository;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();

        // System.setProperty("FIGO_CLIENT_ID", "<use key>");
        // System.setProperty("FIGO_SECRET", "<use secret>");
        System.setProperty("FIGO_TECH_USER", "figo-user");
        System.setProperty("FIGO_TECH_USER_CREDENTIAL", "test123");
    }

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new FigoBanking(BankApi.FIGO));
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new FigoBanking(BankApi.FIGO));
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new FigoBanking(BankApi.FIGO));
    }

    @Test
    public void testFigoPayment() throws Exception {
        UserEntity userEntity = TestUtil.getUserEntity("test-user-id");
        userRepository.save(userEntity);

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id",
            System.getProperty("blz"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(false);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            null, null);
        BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
            .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals("12324463"))
            .findFirst().get();

        bookingService.syncBookings(FINALISED, bankAccessEntity, bankAccountEntitity, BankApi.FIGO, null);

        SinglePaymentEntity paymentEntity = new SinglePaymentEntity();
        paymentEntity.setReceiverIban("receiver_iban_needed_here");
        paymentEntity.setReceiver("Alexander Geist");
        paymentEntity.setAmount(new BigDecimal(1));
        paymentEntity.setPurpose("test");

        paymentService.createSinglePayment(bankAccessEntity, null, null, paymentEntity);
        paymentService.submitSinglePayment(paymentEntity, bankAccessEntity, null, "tan_needed_here");
    }
}
