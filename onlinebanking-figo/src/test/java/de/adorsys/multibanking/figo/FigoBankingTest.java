package de.adorsys.multibanking.figo;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created on 2019-07-24.
 *
 * @author mafo
 */
public class FigoBankingTest {

    private static final String FIGO_TEST_USER = "demo";
    private static final String FIGO_TEST_PIN = "demo";
    private static final String FIGO_TEST_BANKCODE = "90090042";

    private FigoBanking service;

    @Before
    public void init() {
        service = new FigoBanking(BankApi.FIGO);
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void registerUser_should_successfully_run() {
        val bankaccess = getFigoTestUser();

        val result = service.registerUser(bankaccess, "1234");

        assertThat(result, allOf(
            hasProperty("apiUserId", notNullValue()),
            hasProperty("apiPassword", notNullValue()),
            hasProperty("bankApi", is(BankApi.FIGO))
        ));
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void registerUser_should_successfully_run_twice() {
        val bankaccess = getFigoTestUser();

        service.registerUser(bankaccess, "1234");
        val result2 = service.registerUser(bankaccess, "1234");

        assertThat(result2, allOf(
            hasProperty("apiUserId", notNullValue()),
            hasProperty("apiPassword", notNullValue()),
            hasProperty("bankApi", is(BankApi.FIGO))
        ));

    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void removeUser_should_successfully_run_after_registerUser() {
        val bankaccess = getFigoTestUser();
        val result = service.registerUser(bankaccess, "1234");

        service.removeUser(result);

        // success
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void loadBankAccounts_should_successfully_run_after_registerUser() {
        val bankaccess = getFigoTestUser();
        val result = service.registerUser(bankaccess, "1234");
        val request = LoadAccountInformationRequest.builder()
            .bankAccess(bankaccess)
            .bankApiUser(result)
            .bankCode(FIGO_TEST_BANKCODE)
            .storePin(false)
            .pin(FIGO_TEST_PIN)
            .build();

        val response = service.loadBankAccounts(null, request);

        assertThat(response, allOf(
            hasProperty("bankAccounts", hasSize(3))
        ));
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void loadBookings_should_successfully_run_after_registerUser() {
        val bankaccess = getFigoTestUser();
        val result = service.registerUser(bankaccess, "1234");
        val request = LoadAccountInformationRequest.builder()
            .bankAccess(bankaccess)
            .bankApiUser(result)
            .bankCode(FIGO_TEST_BANKCODE)
            .storePin(false)
            .pin(FIGO_TEST_PIN)
            .build();
        val result2 = service.loadBankAccounts(null, request);
        val request2 = LoadBookingsRequest.builder()
            .bankAccess(bankaccess)
            .bankApiUser(result)
            .bankCode(FIGO_TEST_BANKCODE)
            .pin(FIGO_TEST_PIN)
            .bankAccount(result2.getBankAccounts().get(0))
            .build();

        val response = service.loadBookings(null, request2);

        assertThat(response, allOf(
            hasProperty("bookings", hasSize(85)),
            hasProperty("standingOrders", hasSize(0)),
            hasProperty("bankAccountBalance", allOf(
                hasProperty("readyBalance", allOf(
                    hasProperty("amount", is(new BigDecimal(3250.31)))
                ))
            ))
        ));
    }

    private BankAccess getFigoTestUser() {
        val bankAccess = new BankAccess();
        bankAccess.setBankLogin(FIGO_TEST_USER);
        bankAccess.setBankCode(FIGO_TEST_BANKCODE);
        return bankAccess;
    }

}
