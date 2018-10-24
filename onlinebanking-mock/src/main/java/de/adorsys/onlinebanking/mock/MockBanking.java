package de.adorsys.onlinebanking.mock;

import domain.*;
import domain.request.CreateConsentRequest;
import domain.request.LoadAccountInformationRequest;
import domain.request.LoadBookingsRequest;
import domain.request.SubmitPaymentRequest;
import domain.response.LoadAccountInformationResponse;
import domain.response.LoadBookingsResponse;
import org.adorsys.envutils.EnvProperties;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import spi.OnlineBankingService;
import utils.Utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private String mockConnectionUrl = null;

    public MockBanking() {
        mockConnectionUrl = EnvProperties.getEnvOrSysProp("mockConnectionUrl", "https://multibanking-datev-mock.cloud.adorsys.de");
    }

    @Override
    public BankApi bankApi() {
        return BankApi.MOCK;
    }

    @Override
    public boolean externalBankAccountRequired() {
        return false;
    }

    @Override
    public boolean bankSupported(String bankCode) {
        return true;
    }

    @Override
    public boolean bookingsCategorized() {
        return false;
    }

    @Override
    public Object createPayment(Optional<String> bankingUrl, PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public Object deletePayment(Optional<String> bankingUrl, PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public String submitPayment(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public String submitDelete(Optional<String> bankingUrl, SubmitPaymentRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public boolean accountInformationConsentRequired(BankApiUser bankApiUser, String accountReference) {
        return false;
    }

    @Override
    public void createAccountInformationConsent(Optional<String> bankingUrl, CreateConsentRequest startScaRequest) {

    }

    @Override
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(Optional<String> bankingUrl, BankAccess bankAccess, String pin) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(Optional<String> bankingUrl, BankApiUser bankApiUser) {
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(Optional<String> bankingUrl, LoadAccountInformationRequest loadAccountInformationRequest) {
        RestTemplate restTemplate = getRestTemplate(loadAccountInformationRequest.getBankAccess().getBankLogin(),
                loadAccountInformationRequest.getBankAccess().getBankCode(), loadAccountInformationRequest.getPin());

        BankAccount[] bankAccounts = restTemplate.getForObject(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts",
                BankAccount[].class, loadAccountInformationRequest.getBankCode());


        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.bankName(loadAccountInformationRequest.getBankAccess().getBankName());
            bankAccount.externalId(bankApi(), UUID.randomUUID().toString());
        }
        return LoadAccountInformationResponse.builder()
                .bankAccounts(Arrays.asList(bankAccounts))
                .build();
    }


    @Override
    public void removeBankAccount(Optional<String> bankingUrl, BankAccount bankAccount, BankApiUser bankApiUser) {
        // getRestTemplate(bankApiUser.getApiUserId()).delete(mockConnectionUrl+"/bankaccesses/{bankcode}/accounts/{iban}",bankAccount.getBlz(),bankAccount.getIban());
    }

    @Override
    public LoadBookingsResponse loadBookings(Optional<String> bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        BankAccess bankAccess = loadBookingsRequest.getBankAccess();
        BankAccount bankAccount = loadBookingsRequest.getBankAccount();

        List<Booking> bookingList = getRestTemplate(bankAccess.getBankLogin(), loadBookingsRequest.getBankCode(), loadBookingsRequest.getPin()).exchange(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}/bookings",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Booking>>() {
                },
                loadBookingsRequest.getBankCode(),
                bankAccount.getIban()).getBody();

        bookingList.forEach(booking -> {
            booking.setCreditorId((Utils.extractCreditorId(booking.getUsage())));
            booking.setMandateReference(Utils.extractMandateReference(booking.getUsage()));
        });

        return LoadBookingsResponse.builder()
                .bookings(bookingList)
                .standingOrders(getStandingOders(bankAccess, loadBookingsRequest.getPin(), bankAccount.getIban()))
                .bankAccountBalance(getBalance(bankAccess, loadBookingsRequest.getPin(), bankAccount.getIban()))
                .build();
    }

    private List<StandingOrder> getStandingOders(BankAccess ba, String pin, String iban) {
        return getRestTemplate(ba.getBankLogin(), ba.getBankCode(), pin)
                .exchange(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}/standingorders",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<StandingOrder>>() {
                        },
                        ba.getBankCode(),
                        iban).getBody();
    }

    private BankAccountBalance getBalance(BankAccess ba, String pin, String iban) {
        BankAccount account = getRestTemplate(ba.getBankLogin(), ba.getBankCode(), pin)
                .getForObject(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}",
                        BankAccount.class,
                        ba.getBankCode(),
                        iban);
        return new BankAccountBalance().readyHbciBalance(account.getBankAccountBalance().getReadyHbciBalance());
    }



    public RestTemplate getRestTemplate(String bankLogin, String bankCode, String pin) {
        String basicToken = new Base64().encodeAsString((bankLogin + "_" + bankCode + ":" + pin).getBytes(Charset.forName("UTF-8")));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(basicToken));
        return restTemplate;
    }
}
