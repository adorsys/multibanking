package de.adorsys.onlinebanking.mock;

import domain.*;
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
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest loadAccountInformationRequest) {
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
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
        // getRestTemplate(bankApiUser.getApiUserId()).delete(mockConnectionUrl+"/bankaccesses/{bankcode}/accounts/{iban}",bankAccount.getBlz(),bankAccount.getIban());
    }

    @Override
    public LoadBookingsResponse loadBookings(LoadBookingsRequest loadBookingsRequest) {
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

    @Override
    public void createPayment(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, Payment payment) {
    }

    @Override
    public void submitPayment(Payment payment, String pin, String tan) {
    }

    public RestTemplate getRestTemplate(String bankLogin, String bankCode, String pin) {
        String basicToken = new Base64().encodeAsString((bankLogin + "_" + bankCode + ":" + pin).getBytes(Charset.forName("UTF-8")));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(basicToken));
        return restTemplate;
    }
}
