package de.adorsys.multibanking.web;

import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import de.adorsys.multibanking.web.base.entity.PaymentID;
import de.adorsys.multibanking.web.base.entity.PaymentLocation;
import de.adorsys.multibanking.web.base.entity.PaymentRequest;
import de.adorsys.multibanking.web.base.entity.UserDataStructure;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDate;

/**
 * Created by peter on 22.05.18 at 08:04.
 */
public class MB_007_PaymentsTest extends MB_BaseTest {
    private final static String PAYMENT_URI = "/api/v1/bankaccesses/{accessId}/accounts/{accountId}/payments";
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_007_PaymentsTest.class);

    @Test
    public void createPayment() {
        LOGGER.debug("createPayment");
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        createPayment(this, location);
    }

    @Test
    public void createPaymentAndLoadById() {
        LOGGER.debug("createPayment");
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        PaymentID paymentID = createPayment(this, location);

        UserDataStructure userDataStructure = MB_004_BankAccessTest.loadUserDataStructure(this, location);
        BankAccessID firstBankAccessID = userDataStructure.getBankAccessIDs().get(0);
        BankAccountID firstBankAccountID = userDataStructure.getBankAccountIDs(firstBankAccessID).get(0);
        loadPayment(this, firstBankAccessID, firstBankAccountID, paymentID);
    }

    public PaymentID createPayment(MB_BaseTest base, URI location) {
        UserDataStructure userDataStructure = MB_004_BankAccessTest.loadUserDataStructure(this, location);
        BankAccessID firstBankAccessID = userDataStructure.getBankAccessIDs().get(0);
        BankAccountID firstBankAccountID = userDataStructure.getBankAccountIDs(firstBankAccessID).get(0);

        URI uri = paymentsPath(this, firstBankAccessID, firstBankAccountID);
        this.setNextExpectedStatusCode(201);
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPin(PIN);
        PaymentRequest.PaymentRequestBody prb = paymentRequest.getPayment();
        prb.setAmount(321);
        prb.setCycle("WEEKLY");
        prb.setExecutionDay(1);
        prb.setFirstExecutionDate(UserDataStructure.getStringFromLocalDate(LocalDate.now().plusDays(1)));
        prb.setLastExecutionDate(UserDataStructure.getStringFromLocalDate(LocalDate.now().plusYears(1)));
        prb.setPaymentType("TRANSFER");
        prb.setPurpose("testueberweisung");
        prb.setReceiver("PETER");
        prb.setReceiverIban("DE99199999991010101011");

        URI paymentUri = this.testRestTemplate.postForLocation(uri, paymentRequest);
        Assert.assertNotNull(paymentUri);
        String paymentData = this.testRestTemplate.getForObject(paymentUri, String.class);
        LOGGER.debug(UserDataStructure.formatJson(paymentData));


        PaymentLocation paymentLocation = new PaymentLocation(paymentUri);
        return paymentLocation.getPaymentID();
    }

    public static String loadPayment(MB_BaseTest base, BankAccessID bankAccessID, BankAccountID bankAccountID, PaymentID paymentID) {
        base.setNextExpectedStatusCode(200);
        URI paymentLocation = paymentPath(base, bankAccessID, bankAccountID, paymentID);
        String paymentData = base.testRestTemplate.getForObject(paymentLocation, String.class);
        String nicePaymentData = UserDataStructure.formatJson(paymentData);
        LOGGER.debug(nicePaymentData);
        return nicePaymentData;
    }


    public static URI paymentsPath(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID) {
        return base.path(PAYMENT_URI).build(accessID.getValue(), bankAccountID.getValue());
    }

    public static URI paymentPath(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID, PaymentID paymentID) {
        return base.path(PAYMENT_URI).pathSegment(paymentID.getValue()).build(accessID.getValue(), bankAccountID.getValue());
    }
}
