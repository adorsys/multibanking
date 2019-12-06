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
import de.adorsys.multibanking.domain.response.AbstractResponse;
import de.adorsys.multibanking.domain.response.PaymentResponse;
import de.adorsys.multibanking.domain.response.PaymentStatusResponse;
import de.adorsys.multibanking.domain.transaction.*;
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
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kapott.hbci.manager.HBCIVersion.HBCI_300;

@Slf4j
public class HbciIntegrationIT {

    private static final String SCA_METHOD_ID = "901";

    private String iban;
    private String psuId;
    private String psuCorporateId;
    private String pin;

    private HbciBanking hbci4JavaBanking = new HbciBanking(null);

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
        this.psuId = System.getProperty("login", "psd2test2");
        this.psuCorporateId = System.getProperty("login2", "psuCorporateId");
        this.pin = System.getProperty("pin", "pin");

        BankInfo bankInfo = new BankInfo();
        bankInfo.setBlz(Iban.valueOf(iban).getBankCode());
        bankInfo.setPinTanAddress(System.getProperty("bankUrl"));
        bankInfo.setPinTanVersion(HBCI_300);
        bankInfo.setBic(System.getProperty("bic"));
        HBCIUtils.addBankInfo(bankInfo);
    }

    @Test
    public void hbciSinglePayment() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(bankAccount);

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciFuturePayment() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        FutureSinglePayment payment = new FutureSinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(bankAccount);
        payment.setExecutionDate(LocalDate.now().plusDays(2));

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());
        assertThat(paymentResponse.getTransactionId()).isNotBlank();
    }

    @Test
    public void hbciInstantPayment() throws InterruptedException {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test130");
        payment.setPsuAccount(bankAccount);
        payment.setInstantPayment(true);

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        assertThat(paymentResponse.getTransactionId()).isNotBlank();

        Thread.sleep(31000L);
        PaymentStatusResponse paymentStatusResponse = hbciInstantPaymentStatus(paymentResponse.getTransactionId());
        assertThat(paymentStatusResponse).isNotNull();
    }

    private PaymentStatusResponse hbciInstantPaymentStatus(String instantPaymentId) {
        TanTransportType tanTransportType = new TanTransportType();
        tanTransportType.setId(SCA_METHOD_ID);

        HbciConsent hbciConsent = new HbciConsent();
        hbciConsent.setSelectedMethod(tanTransportType);
        hbciConsent.setCredentials(Credentials.builder()
            .userId(psuCorporateId)
            .customerId(psuId)
            .pin(pin)
            .build());

        BankAccess bankAccess = new BankAccess();
        bankAccess.setBankCode(Iban.valueOf(iban).getBankCode());

        Bank bank = new Bank();
        bank.setBankCode(bankAccess.getBankCode());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        PaymentStatusReqest paymentStatusReqest = new PaymentStatusReqest();
        paymentStatusReqest.setPsuAccount(bankAccount);
        paymentStatusReqest.setPaymentId(instantPaymentId);

        TransactionRequest<PaymentStatusReqest> transactionRequest =
            TransactionRequestFactory.create(paymentStatusReqest, null, bankAccess, bank, hbciConsent);

        PaymentStatusResponse paymentStatus =
            hbci4JavaBanking.getStrongCustomerAuthorisation().getPaymentStatus(transactionRequest);

        return paymentStatus;
    }

    @Test
    public void hbciPayment() {
        FutureSinglePayment payment = new FutureSinglePayment();
        payment.setReceiverIban("DE56760905000002257793");
        payment.setReceiver("Alexander Geist");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test130");

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        payment.setPsuAccount(bankAccount);
        payment.setExecutionDate(LocalDate.now().plusDays(10));

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciDTAZV() throws Exception {
        URL testDtazvUrl = Objects.requireNonNull(HbciIntegrationIT.class.getClassLoader().getResource("test-dtazv" +
            ".txt")).toURI().toURL();

        ForeignPayment payment = new ForeignPayment();
        payment.setDtazv(readFile(testDtazvUrl.getPath()));

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        payment.setPsuAccount(bankAccount);

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciDeleteStandingOrder() {
        StandingOrderRequest standingOrder = new StandingOrderRequest();
        standingOrder.setAmount(new BigDecimal("12.00"));
        standingOrder.setUsage("Test zum Löschen");
        standingOrder.setCycle(Frequency.MONTHLY);
        standingOrder.setOrderId("371723082046354727707F6TDWQPHE");
        standingOrder.setExecutionDay(3);
        standingOrder.setFirstExecutionDate(LocalDate.now().withDayOfMonth(3).plusMonths(1));
        standingOrder.setLastExecutionDate(LocalDate.now().withDayOfMonth(3).plusMonths(8));
        standingOrder.setDelete(true);

        BankAccount debtorAccount = new BankAccount();
        debtorAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        debtorAccount.setIban(iban);
        standingOrder.setPsuAccount(debtorAccount);

        BankAccount other = new BankAccount();
        other.setIban("DE56760905000002257793");
        other.setOwner("Moriz Mustermann");
        standingOrder.setOtherAccount(other);

        PaymentResponse paymentResponse = hbciSubmitTransaction(standingOrder);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse).isNotNull();
    }

    @Test
    public void hbciTransfer() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        SinglePayment payment = new SinglePayment();
        payment.setReceiverIban("DE51250400903312345678");
        payment.setReceiver("Selber");
        payment.setAmount(new BigDecimal("12.00"));
        payment.setPurpose("test umbuchung");
        payment.setPsuAccount(bankAccount);

        HbciConsent hbciConsent = new HbciConsent();
        hbciConsent.setCredentials(Credentials.builder()
            .userId(psuCorporateId)
            .customerId(psuId)
            .pin(pin)
            .build());

        BankAccess bankAccess = new BankAccess();
        bankAccess.setBankCode(Iban.valueOf(iban).getAccountNumber());

        Bank bank = new Bank();
        bank.setBankCode(bankAccess.getBankCode());

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

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(Iban.valueOf(iban).getAccountNumber());
        bankAccount.setIban(iban);

        payment.setPsuAccount(bankAccount);

        PaymentResponse paymentResponse = hbciSubmitTransaction(payment);
        log.info("Order-ID: {}", paymentResponse.getTransactionId());

        assertThat(paymentResponse.getTransactionId()).isNotNull();
    }

    private PaymentResponse hbciSubmitTransaction(AbstractPayment payment) {
        TanTransportType tanTransportType = new TanTransportType();
        tanTransportType.setId(SCA_METHOD_ID);

        HbciConsent hbciConsent = new HbciConsent();
        hbciConsent.setSelectedMethod(tanTransportType);
        hbciConsent.setCredentials(Credentials.builder()
            .userId(psuCorporateId)
            .customerId(psuId)
            .pin(pin)
            .build());

        BankAccess bankAccess = new BankAccess();
        bankAccess.setBankCode(Iban.valueOf(iban).getBankCode());

        Bank bank = new Bank();
        bank.setBankCode(bankAccess.getBankCode());

        TransactionRequest<AbstractPayment> transactionRequest =
            TransactionRequestFactory.create(payment, null, bankAccess, bank, hbciConsent);

        AbstractResponse response = hbci4JavaBanking.executePayment(transactionRequest);
        hbciConsent.setHbciTanSubmit(response.getAuthorisationCodeResponse().getTanSubmit());

        // break here to enter tan
        String tan = "enterTan";
        TransactionAuthorisationRequest transactionAuthorisationRequest = new TransactionAuthorisationRequest(tan);
        transactionAuthorisationRequest.setBankApiConsentData(hbciConsent);
        hbci4JavaBanking.getStrongCustomerAuthorisation().authorizeConsent(transactionAuthorisationRequest);

        return hbci4JavaBanking.executePayment(transactionRequest);
    }
}
