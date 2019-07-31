package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class HbciBulkPaymentTest {

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private BankRepositoryIf bankRepository;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new Hbci4JavaBanking());
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new Hbci4JavaBanking());
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new Hbci4JavaBanking());

        bankRepository.findByBankCode(System.getProperty("blz")).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", System.getProperty("blz"), BankApi.HBCI);
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    @Test
    public void testPayment() throws Exception {

        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id",
            System.getProperty("blz"), System.getProperty("pin"));
        bankAccessEntity.setBankLogin(System.getProperty("login"));
        bankAccessEntity.setBankLogin2(System.getProperty("login2"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(true);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankAccessEntity,
            BankApi.HBCI);
        BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
            .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals(System.getProperty("account")))
            .findFirst().get();
        bankAccountEntitity.setId("test-account-id");

        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal(12.00));
        payment.setPurpose("test130");

        log.info("------------ " + bankAccessEntity.getTanTransportTypes().get(BankApi.HBCI).get(5).toString());

        bankAccessEntity.getTanTransportTypes().get(BankApi.HBCI).forEach(tanTransportType -> log.info(tanTransportType.toString()));

        TanTransportType tanTransportType = bankAccessEntity.getTanTransportTypes().get(BankApi.HBCI).get(5);

        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(payment));
        bulkPayment.setDebtorBankAccount(bankAccountEntitity);

        BulkPaymentEntity paymentEntity = paymentService.createBulkPayment(bankAccessEntity,
            tanTransportType, System.getProperty("pin"), bulkPayment);

        String tan = "";
        paymentService.submitBulkPayment(paymentEntity, bankAccessEntity, System.getProperty("pin"), tan);

    }

}
