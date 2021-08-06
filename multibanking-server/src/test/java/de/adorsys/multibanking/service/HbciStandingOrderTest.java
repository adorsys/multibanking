package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.transaction.PeriodicPayment;
import de.adorsys.multibanking.hbci.HbciBanking;
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
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class HbciStandingOrderTest {

    @Autowired
    private StandingOrderService standingOrderService;
    @Autowired
    private BankRepositoryIf bankRepository;
    @Autowired
    private BankAccountService bankAccountService;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;

    private BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", System.getProperty("blz"), BankApi.HBCI);

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(new HbciBanking(null, 0, 0, 0));
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(new HbciBanking(null, 0, 0, 0));
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(new HbciBanking(null, 0, 0, 0));

        bankRepository.findByBankCode(System.getProperty("blz")).orElseGet(() -> {
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    @Test
    public void testListStandingOrders() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id",
            System.getProperty("blz"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(true);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankEntity,
            bankAccessEntity,
            BankApi.HBCI);
        BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
            .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals(System.getProperty("account")))
            .findFirst().get();
        bankAccountEntitity.setId("test-account-id");

        //TODO hbci call for standing orders needed
    }

    @Test
    public void testNewStandingOrder() {
        BankAccessEntity bankAccessEntity = TestUtil.getBankAccessEntity("test-user-id", "test-access-id",
            System.getProperty("blz"));
        bankAccessEntity.setCategorizeBookings(false);
        bankAccessEntity.setStoreAnalytics(false);

        List<BankAccountEntity> bankAccountEntities = bankAccountService.loadBankAccountsOnline(bankEntity,
            bankAccessEntity,
            BankApi.HBCI);
        BankAccountEntity bankAccountEntitity = bankAccountEntities.stream()
            .filter(bankAccountEntity -> bankAccountEntity.getAccountNumber().equals("3312345678"))
            .findFirst().get();

        PeriodicPayment standingOrder = new PeriodicPayment();
        standingOrder.setOtherAccount(new BankAccount());
        standingOrder.getOtherAccount().setIban("DE56760905000002257793");
        standingOrder.getOtherAccount().setOwner("Alexander Geist");
        standingOrder.setAmount(new BigDecimal(100));
        standingOrder.getOtherAccount().setBic("PBNKDEFF");
        standingOrder.setUsage("Dauerauftrag Test4");

        standingOrder.setCycle(Frequency.MONTHLY);
        standingOrder.setExecutionDay(1);
        standingOrder.setFirstExecutionDate(LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()));
        standingOrder.setLastExecutionDate(LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).plusYears(2));
        standingOrder.setPsuAccount(bankAccountEntitity);

        Object tanSubmit = standingOrderService.createStandingOrder(bankAccessEntity, null,
            standingOrder);

        String tan = "";
        standingOrderService.submitStandingOrder(standingOrder, tanSubmit, bankAccessEntity, null, tan);
    }
}
