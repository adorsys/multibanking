package de.adorsys.multibanking.figo;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankApi;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
        val result = service.registerUser(FIGO_TEST_USER);

        assertThat(result, allOf(
            hasProperty("apiUserId", notNullValue()),
            hasProperty("apiPassword", notNullValue()),
            hasProperty("bankApi", is(BankApi.FIGO))
        ));
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void registerUser_should_successfully_run_twice() {
        service.registerUser(FIGO_TEST_USER);
        val result2 = service.registerUser(FIGO_TEST_USER);

        assertThat(result2, allOf(
            hasProperty("apiUserId", notNullValue()),
            hasProperty("apiPassword", notNullValue()),
            hasProperty("bankApi", is(BankApi.FIGO))
        ));

    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void removeUser_should_successfully_run_after_registerUser() {
        val result = service.registerUser(FIGO_TEST_USER);

        service.removeUser(result);

        // success
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void loadBankAccounts_should_successfully_run_after_registerUser() {
        val bankaccess = getFigoTestUser();
        val result = service.registerUser(FIGO_TEST_USER);

//        val request = new LoadAccountInformationRequest();
//        request.setBankAccess(bankaccess);
//        request.setBankApiUser(result);
//        request.setBankCode(FIGO_TEST_BANKCODE);
//
//        val response = service.loadBankAccounts(request);
//
//        assertThat(response, allOf(
//            hasProperty("bankAccounts", hasSize(3))
//        ));
    }

    @Ignore("Use System Variables e.g. from de.adorsys.multibanking.service.FigoPaymentTest")
    @Test
    public void loadBookings_should_successfully_run_after_registerUser() {
        val bankaccess = getFigoTestUser();
        val result = service.registerUser(FIGO_TEST_USER);
//        val request = new LoadAccountInformationRequest();
//        request.setBankAccess(bankaccess);
//        request.setBankApiUser(result);
//        request.setBankCode(FIGO_TEST_BANKCODE);

//        val result2 = service.loadBankAccounts(request);
//        val request2 = new LoadBookingsRequest();
//        request2.setBankAccess(bankaccess);
//        request2.setBankApiUser(result);
//        request2.setBankCode(FIGO_TEST_BANKCODE);
//        request2.setCredentials(Credentials.builder().pin(FIGO_TEST_PIN).build());
//        request2.setBankAccount(result2.getBankAccounts().get(0));

//        val response = service.loadBookings(request2);

//        assertThat(response, allOf(
//            hasProperty("bookings", hasSize(85)),
//            hasProperty("standingOrders", hasSize(0)),
//            hasProperty("bankAccountBalance", allOf(
//                hasProperty("readyBalance", allOf(
//                    hasProperty("amount", is(new BigDecimal(3250.31)))
//                ))
//            ))
//        ));
    }

    private BankAccess getFigoTestUser() {
        val bankAccess = new BankAccess();
        bankAccess.setBankCode(FIGO_TEST_BANKCODE);
        return bankAccess;
    }

}
