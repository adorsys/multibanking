package de.adorsys.multibanking.service.old;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
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
import de.adorsys.multibanking.domain.SinglePaymentEntity;
import de.adorsys.multibanking.service.BankAccountService;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.service.PaymentService;
import de.adorsys.multibanking.service.UserDataService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import domain.BankApi;
import domain.SinglePayment;
import hbci4java.Hbci4JavaBanking;

/**
 * Created by alexg on 09.10.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"InMemory"})
@Ignore
public class HbciPaymentTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserDataService uds;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

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
    public void testPayment() {
        try {
            // TODO inject user credentials
            // TODO: inject UserIdAuth
//        	UserEntity userEntity = TestUtil.getUserEntity("test-user-id");
            uds.createUser(null);

            BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id", System.getProperty("blz"), System.getProperty("pin"));
            bankAccessEntity.setBankLogin(System.getProperty("login"));
            bankAccessEntity.setCategorizeBookings(false);
            bankAccessEntity.setStoreAnalytics(false);

            List<BankAccountEntity> bankAccountEntities = uds.load().bankAccessDataOrException(bankAccessEntity.getId()).bankAccountEntityAsList();
//            List<BankAccountEntity> bankAccountEntities = bankAccountService.loadForBankAccess(bankAccessEntity.getId());
            BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
                    .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals("2257793"))
                    .findFirst().get();

            bookingService.syncBookings(bankAccessEntity.getId(), bankAccountEntitity.getId(), BankApi.HBCI, System.getProperty("pin"));

            SinglePayment payment = new SinglePayment();
            payment.setReceiverIban("DE56760905000002257793");
            payment.setReceiver("Alexander Geist");
            payment.setAmount(new BigDecimal(1));
            payment.setPurpose("test129");

            // "test-user-id", 
            SinglePaymentEntity paymentEntity = paymentService.createPayment(bankAccessEntity, bankAccountEntitity, System.getProperty("pin"), payment);

            String tan = "";
            paymentService.submitPayment(paymentEntity, bankAccessEntity.getBankCode(), tan);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
