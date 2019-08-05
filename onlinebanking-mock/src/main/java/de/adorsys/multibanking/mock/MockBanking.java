package de.adorsys.multibanking.mock;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.LoadBookingsRequest;
import de.adorsys.multibanking.domain.request.SubmitAuthorizationCodeRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.response.LoadBookingsResponse;
import de.adorsys.multibanking.domain.response.SubmitAuthorizationCodeResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.StandingOrder;
import de.adorsys.multibanking.domain.utils.Utils;
import org.adorsys.envutils.EnvProperties;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private String mockConnectionUrl;

    public MockBanking() {
        mockConnectionUrl = EnvProperties.getEnvOrSysProp("mockConnectionUrl", "https://multibanking-datev-mock.cloud" +
            ".adorsys.de");
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
    public AuthorisationCodeResponse requestAuthorizationCode(String bankingUrl, TransactionRequest paymentRequest) {
        return null;
    }

    @Override
    public SubmitAuthorizationCodeResponse submitAuthorizationCode(SubmitAuthorizationCodeRequest submitPaymentRequest) {
        return null;
    }

    @Override
    public boolean psd2Scope() {
        return false;
    }

    @Override
    public boolean userRegistrationRequired() {
        return false;
    }

    @Override
    public BankApiUser registerUser(BankAccess bankAccess, String pin) {
        //no registration needed
        return null;
    }

    @Override
    public void removeUser(BankApiUser bankApiUser) {
    }

    @Override
    public LoadAccountInformationResponse loadBankAccounts(String bankingUrl,
                                                           LoadAccountInformationRequest loadAccountInformationRequest) {
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
        // getRestTemplate(bankApiUser.getApiUserId()).delete
        // (mockConnectionUrl+"/bankaccesses/{bankcode}/accounts/{iban}",bankAccount.getBlz(),bankAccount.getIban());
    }

    @Override
    public LoadBookingsResponse loadBookings(String bankingUrl, LoadBookingsRequest loadBookingsRequest) {
        BankAccess bankAccess = loadBookingsRequest.getBankAccess();
        BankAccount bankAccount = loadBookingsRequest.getBankAccount();

        List<Booking> bookingList = getRestTemplate(bankAccess.getBankLogin(), loadBookingsRequest.getBankCode(),
            loadBookingsRequest.getPin()).exchange(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban" +
                "}/bookings",
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

    private BalancesReport getBalance(BankAccess ba, String pin, String iban) {
        BankAccount account = getRestTemplate(ba.getBankLogin(), ba.getBankCode(), pin)
            .getForObject(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}",
                BankAccount.class,
                ba.getBankCode(),
                iban);
        return account.getBalances();
    }

    private RestTemplate getRestTemplate(String bankLogin, String bankCode, String pin) {
        String basicToken =
            new Base64().encodeAsString((bankLogin + "_" + bankCode + ":" + pin).getBytes(Charset.forName("UTF-8")));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(basicToken));
        return restTemplate;
    }
}
