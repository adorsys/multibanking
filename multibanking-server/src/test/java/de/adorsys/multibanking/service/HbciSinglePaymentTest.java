package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ConsentAuthorisationRequiredException;
import de.adorsys.multibanking.hbci.Hbci4JavaBanking;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class HbciSinglePaymentTest {

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private BankRepositoryIf bankRepository;

    @MockBean
    private OnlineBankingServiceProducer bankingServiceProducer;
    @Value("${banks.config.url:classpath:/blz-test.properties}")
    private URL banksConfigUrl;

    @BeforeClass
    public static void beforeClass() {
        TestConstants.setup();
    }

    @Before
    public void beforeTest() throws IOException {
        Hbci4JavaBanking hbci4JavaBanking = new Hbci4JavaBanking(banksConfigUrl.openStream(), true);

        MockitoAnnotations.initMocks(this);
        when(bankingServiceProducer.getBankingService(anyString())).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.FIGO)).thenReturn(hbci4JavaBanking);
        when(bankingServiceProducer.getBankingService(BankApi.HBCI)).thenReturn(hbci4JavaBanking);

        bankRepository.findByBankCode(System.getProperty("blz")).orElseGet(() -> {
            BankEntity bankEntity = TestUtil.getBankEntity("Test Bank", System.getProperty("blz"), BankApi.HBCI);
            bankRepository.save(bankEntity);
            return bankEntity;
        });
    }

    public void testSinglePayment() throws Exception {
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

        TanTransportType tanTransportType = bankAccessEntity.getTanTransportTypes().get(BankApi.HBCI).stream()
            .filter(ttt -> StringUtils.containsIgnoreCase(ttt.getName(), "sms"))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("invalid tan transport type"));

        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal(12.00));
        payment.setPurpose("test130");
        payment.setDebtorBankAccount(bankAccountEntitity);

        SinglePaymentEntity paymentEntity = paymentService.createSinglePayment(bankAccessEntity, tanTransportType,
            System.getProperty("pin"), payment);

        String tan = "";
        paymentService.submitSinglePayment(paymentEntity, bankAccessEntity, System.getProperty("pin"), tan);
    }

    @Test
    public void testRawPayment() throws ConsentAuthorisationRequiredException {
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

        TanTransportType tanTransportType = bankAccessEntity.getTanTransportTypes().get(BankApi.HBCI).stream()
            .filter(ttt -> StringUtils.containsIgnoreCase(ttt.getName(), "sms"))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("invalid tan transport type"));

        String painXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Document " +
            "xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\" xmlns:xsi=\"http://www.w3" +
            ".org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03 " +
            "pain.001.001.03.xsd\"><CstmrCdtTrfInitn><GrpHdr><MsgId>2019-01-24T13:10:03:0705</MsgId><CreDtTm>2019" +
            "-01-24T13:10:08</CreDtTm><NbOfTxs>1</NbOfTxs><CtrlSum>12</CtrlSum><InitgPty><Nm>TESTKONTO</Nm" +
            "></InitgPty></GrpHdr><PmtInf><PmtInfId>2019-01-24T13:10:03:0705</PmtInfId><PmtMtd>TRF</PmtMtd" +
            "><NbOfTxs>1</NbOfTxs><CtrlSum>12</CtrlSum><PmtTpInf><SvcLvl><Cd>SEPA</Cd></SvcLvl></PmtTpInf" +
            "><ReqdExctnDt>1999-01-01</ReqdExctnDt><Dbtr><Nm>TESTKONTO</Nm></Dbtr><DbtrAcct><Id><IBAN" +
            ">DE51250400903312345678</IBAN></Id></DbtrAcct><DbtrAgt><FinInstnId><BIC>XBANDECG</BIC></FinInstnId" +
            "></DbtrAgt><ChrgBr>SLEV</ChrgBr><CdtTrfTxInf><PmtId><EndToEndId>NOTPROVIDED</EndToEndId></PmtId><Amt" +
            "><InstdAmt Ccy=\"EUR\">12</InstdAmt></Amt><Cdtr><Nm>Alexander " +
            "Geist</Nm></Cdtr><CdtrAcct><Id><IBAN>DE56760905000002257793</IBAN></Id></CdtrAcct><RmtInf><Ustrd" +
            ">test130</Ustrd></RmtInf></CdtTrfTxInf></PmtInf></CstmrCdtTrfInitn></Document>";

        RawSepaPayment payment = new RawSepaPayment();
        payment.setPainXml(painXml);
        payment.setDebtorBankAccount(bankAccountEntitity);

        RawSepaTransactionEntity paymentEntity = paymentService.createSepaRawPayment(bankAccessEntity,
            tanTransportType, System.getProperty("pin"), payment);

        String tan = "";
        paymentService.submitRawSepaTransaction(paymentEntity, bankAccessEntity, System.getProperty("pin"), tan);
    }

}
