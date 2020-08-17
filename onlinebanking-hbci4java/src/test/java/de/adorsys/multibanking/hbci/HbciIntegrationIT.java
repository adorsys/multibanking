/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.TransactionRequestFactory;
import de.adorsys.multibanking.domain.response.*;
import de.adorsys.multibanking.domain.transaction.*;
import de.adorsys.multibanking.hbci.job.VeuListJob;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.adorsys.multibanking.hbci.HbciCacheHandler.createCallback;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kapott.hbci.manager.HBCIVersion.HBCI_300;

@Slf4j
public class HbciIntegrationIT {

    private String scaMethodId;
    private String scaTanMedia;
    private String iban;
    private String psuId;
    private String psuCorporateId;
    private String pin;
    private String psuIdMSCA;
    private String psuCorporateIdMSCA;
    private String pinMSCA;

    private HbciBanking hbci4JavaBanking = new HbciBanking(null, 0, 0);

    private static String readFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), ISO_8859_1)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    @Before
    public void prepareEnv() {
        this.iban = System.getProperty("iban", Iban.random().toString());
        this.scaMethodId = System.getProperty("scaMethodId", "901");
        this.scaTanMedia = System.getProperty("scaTanMedia");
        this.psuId = System.getProperty("login", "psd2test2");
        this.psuCorporateId = System.getProperty("login2", null);
        this.pin = System.getProperty("pin", "pin");
        this.psuIdMSCA = System.getProperty("loginMsca", null);
        this.psuCorporateIdMSCA = System.getProperty("login2Msca", null);
        this.pinMSCA = System.getProperty("pinMsca", null);

        BankInfo bankInfo = Optional.ofNullable(HBCIUtils.getBankInfo(Iban.valueOf(this.iban).getBankCode()))
            .orElseGet(() -> {
                BankInfo newBank = new BankInfo();
                newBank.setBlz(Iban.valueOf(this.iban).getBankCode());
                newBank.setPinTanVersion(HBCI_300);
                newBank.setBic(System.getProperty("bic"));
                HBCIUtils.addBankInfo(newBank);
                return newBank;
            });

        Optional.ofNullable(System.getProperty("bankUrl"))
            .ifPresent(bankInfo::setPinTanAddress);
    }

    @Test
    public void hbciLoadAccounts() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        LoadAccounts loadAccounts = new LoadAccounts();
        loadAccounts.setWithBalances(true);
        TransactionRequest<LoadAccounts> loadAccountsRequest = TransactionRequestFactory.create(loadAccounts, null, bankAccess, bank, hbciConsent);

        AccountInformationResponse response = hbci4JavaBanking.loadBankAccounts(loadAccountsRequest);

        if (response.getAuthorisationCodeResponse() != null) {
            hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());
            hbciConsent.setScaAuthenticationData("12456");

            response = hbci4JavaBanking.loadBankAccounts(loadAccountsRequest);
        }
        assertThat(response.getBankAccounts()).isNotEmpty();
    }

    @Test
    public void hbciLoadBalances() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        LoadBalances loadBalances = new LoadBalances();
        loadBalances.setPsuAccount(createBankAccount());
        TransactionRequest<LoadBalances> loadBalancesRequest = TransactionRequestFactory.create(loadBalances, null, bankAccess, bank, hbciConsent);

        LoadBalancesResponse response = hbci4JavaBanking.loadBalances(loadBalancesRequest);

        if (response.getAuthorisationCodeResponse() != null) {
            hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());
            hbciConsent.setScaAuthenticationData("12456");

            response = hbci4JavaBanking.loadBalances(loadBalancesRequest);
        }

        assertThat(response.getBankAccount().getBalances()).isNotNull();
    }

    @Test
    public void hbciLoadStandingOrders() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        LoadStandingOrders loadStandingOrders = new LoadStandingOrders();
        loadStandingOrders.setPsuAccount(createBankAccount());
        TransactionRequest<LoadStandingOrders> loadStandingOrdersRequest = TransactionRequestFactory.create(loadStandingOrders, null, bankAccess, bank, hbciConsent);

        StandingOrdersResponse response = hbci4JavaBanking.loadStandingOrders(loadStandingOrdersRequest);

        if (response.getAuthorisationCodeResponse() != null) {
            hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());
            hbciConsent.setScaAuthenticationData("12456");

            response = hbci4JavaBanking.loadStandingOrders(loadStandingOrdersRequest);
        }

        assertThat(response.getStandingOrders()).isNotNull();
    }

    @Test
    public void hbciLoadTransactions() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        LoadTransactions loadTransactions = new LoadTransactions();
        loadTransactions.setPsuAccount(createBankAccount());
        TransactionRequest<LoadTransactions> loadTransactionsRequest = TransactionRequestFactory.create(loadTransactions, null, bankAccess, bank, hbciConsent);

        TransactionsResponse response = hbci4JavaBanking.loadTransactions(loadTransactionsRequest);

        if (response.getAuthorisationCodeResponse() != null) {
            hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());
            hbciConsent.setScaAuthenticationData("12456");

            response = hbci4JavaBanking.loadTransactions(loadTransactionsRequest);
        }

        assertThat(response.getBookings()).isNotEmpty();
    }

    @Test
    public void hbciLoadVeuList() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        LoadVeuList loadVeuList = new LoadVeuList();
        loadVeuList.setPsuAccount(createBankAccount());
        TransactionRequest<LoadVeuList> loadVeuListRequest = TransactionRequestFactory.create(loadVeuList, null, bankAccess, bank, hbciConsent);

        HbciBpdUpdCallback hbciCallback = createCallback(loadVeuListRequest);
        VeuListResponse response = new VeuListJob(loadVeuListRequest).execute(hbciCallback);

        System.out.println();
    }

    @Test
    public void hbciRequestTan() {
        TanRequest tanRequest = new TanRequest();
        tanRequest.setPsuAccount(createBankAccount());

        PaymentResponse paymentResponse = hbciInitTransaction(tanRequest);
        paymentResponse = hbciSubmitTransaction(tanRequest, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciSinglePayment() {
        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Maxx Mustermann");
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(createBankAccount());

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse.getTransactionId()).isNotNull();

        if (paymentResponse.containsMessage("0021")) { //Multilevel SCA
            payment = new SinglePayment();
            payment.setPsuAccount(createBankAccount());
            payment.setOrderId(paymentResponse.getTransactionId());
            payment.setVeu2ndSignature(true);
            paymentResponse = hbciInitTransaction(payment);
            paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        }
    }

    private void loadVeuList() {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        TransactionRequest<LoadVeuList> transactionRequest = TransactionRequestFactory.create(new LoadVeuList(), null, bankAccess, bank, hbciConsent);
        VeuListJob veuListJob = new VeuListJob(transactionRequest);
        VeuListResponse execute = veuListJob.execute(createCallback(transactionRequest));
    }

    @Test
    public void hbciFuturePayment() {
        FutureSinglePayment payment = new FutureSinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Maxx Mustermann");
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(createBankAccount());
        payment.setExecutionDate(LocalDate.now().plusDays(2));

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());
        assertThat(paymentResponse.getTransactionId()).isNotBlank();
    }

    @Test
    public void hbciInstantPayment() throws InterruptedException {
        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Maxx Mustermann");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(createBankAccount());
        payment.setInstantPayment(true);

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        assertThat(paymentResponse.getTransactionId()).isNotBlank();

        Thread.sleep(31000L);
        PaymentStatusResponse paymentStatusResponse = hbciInstantPaymentStatus(paymentResponse.getTransactionId());
        assertThat(paymentStatusResponse).isNotNull();
    }

    private PaymentStatusResponse hbciInstantPaymentStatus(String instantPaymentId) {
        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        PaymentStatusReqest paymentStatusReqest = new PaymentStatusReqest();
        paymentStatusReqest.setPsuAccount(createBankAccount());
        paymentStatusReqest.setPaymentId(instantPaymentId);

        TransactionRequest<PaymentStatusReqest> transactionRequest =
            TransactionRequestFactory.create(paymentStatusReqest, null, bankAccess, bank, hbciConsent);

        return hbci4JavaBanking.getStrongCustomerAuthorisation().getPaymentStatus(transactionRequest);
    }

    @Test
    public void hbciPayment() {
        FutureSinglePayment payment = new FutureSinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Maxx Mustermann");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(createBankAccount());
        payment.setExecutionDate(LocalDate.now().plusDays(10));

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciDTAZV() throws Exception {
        URL testDtazvUrl = Objects.requireNonNull(HbciIntegrationIT.class.getClassLoader().getResource("test-dtazv" +
            ".txt")).toURI().toURL();

        ForeignPayment payment = new ForeignPayment();
        payment.setDtazv(readFile(testDtazvUrl.getPath()));
        payment.setPsuAccount(createBankAccount());

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciDeleteStandingOrder() {
        PeriodicPayment standingOrder = new PeriodicPayment();
        standingOrder.setAmount(new BigDecimal("12.00"));
        standingOrder.setUsage("Test zum Löschen");
        standingOrder.setCycle(Frequency.MONTHLY);
        standingOrder.setOrderId("371723082046354727707F6TDWQPHE");
        standingOrder.setExecutionDay(3);
        standingOrder.setFirstExecutionDate(LocalDate.now().withDayOfMonth(3).plusMonths(1));
        standingOrder.setLastExecutionDate(LocalDate.now().withDayOfMonth(3).plusMonths(8));
        standingOrder.setDelete(true);
        standingOrder.setPsuAccount(createBankAccount());

        BankAccount other = new BankAccount();
        other.setIban("DE56760905000002257793");
        other.setOwner("Moriz Mustermann");
        standingOrder.setOtherAccount(other);

        PaymentResponse paymentResponse = hbciInitTransaction(standingOrder);
        paymentResponse = hbciSubmitTransaction(standingOrder, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciTransfer() {
        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE51250400903312345678");
        payment.setReceiver("Selber");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test umbuchung");
        payment.setPsuAccount(createBankAccount());

        HbciConsent hbciConsent = createHbciConsent();
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        TransactionRequest<AbstractPayment> transactionRequest =
            TransactionRequestFactory.create(payment, null, bankAccess, bank, hbciConsent);

        PaymentResponse paymentResponse = hbci4JavaBanking.executePayment(transactionRequest);

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciDeleteFutureBulkPayment() {
        FutureBulkPayment payment = new FutureBulkPayment();
        payment.setExecutionDate(LocalDate.now().plusDays(1));
        payment.setOrderId("Test-Order-Id");
        payment.setDelete(true);
        payment.setPsuAccount(createBankAccount());

        PaymentResponse paymentResponse = hbciInitTransaction(payment);
        paymentResponse = hbciSubmitTransaction(payment, paymentResponse.getBankApiConsentData());
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse.getTransactionId()).isNotNull();
    }

    private PaymentResponse hbciInitTransaction(AbstractPayment payment) {
        HbciConsent hbciConsent = createHbciConsent(payment.isVeu2ndSignature());
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        TransactionRequest<AbstractPayment> transactionRequest =
            TransactionRequestFactory.create(payment, null, bankAccess, bank, hbciConsent);

        PaymentResponse response = hbci4JavaBanking.executePayment(transactionRequest);
        hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());
        return response;
    }

    private PaymentResponse hbciSubmitTransaction(AbstractPayment payment, Object hbciConsent) {
        BankAccess bankAccess = createBankAccess();
        Bank bank = createBank(bankAccess);

        TransactionRequest<AbstractPayment> transactionRequest =
            TransactionRequestFactory.create(payment, null, bankAccess, bank, hbciConsent);

        // break here to enter tan
        String tan = "enterTan";
        TransactionAuthorisationRequest transactionAuthorisationRequest = new TransactionAuthorisationRequest(tan);
        transactionAuthorisationRequest.setBankApiConsentData(hbciConsent);
        hbci4JavaBanking.getStrongCustomerAuthorisation().authorizeConsent(transactionAuthorisationRequest);

        return hbci4JavaBanking.executePayment(transactionRequest);
    }

    private HbciConsent createHbciConsent() {
        return createHbciConsent(false);
    }

    private HbciConsent createHbciConsent(boolean secondSignature) {
        TanTransportType tanTransportType = new TanTransportType();
        tanTransportType.setId(scaMethodId);
        tanTransportType.setMedium(scaTanMedia);

        HbciConsent hbciConsent = new HbciConsent();
        hbciConsent.setSelectedMethod(tanTransportType);
        hbciConsent.setCredentials(Credentials.builder()
            .userId(secondSignature ? psuCorporateIdMSCA : psuCorporateId)
            .customerId(secondSignature ? psuIdMSCA : psuId)
            .pin(secondSignature ? pinMSCA : pin)
            .build());
        return hbciConsent;
    }

    private BankAccess createBankAccess() {
        BankAccess bankAccess = new BankAccess();
        bankAccess.setBankCode(Iban.valueOf(iban).getBankCode());
        return bankAccess;
    }

    private Bank createBank(BankAccess bankAccess) {
        Bank bank = new Bank();
        bank.setBankCode(bankAccess.getBankCode());
        return bank;
    }

    private BankAccount createBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);
        return bankAccount;
    }
}
