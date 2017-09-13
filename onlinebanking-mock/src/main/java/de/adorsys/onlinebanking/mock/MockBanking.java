package de.adorsys.onlinebanking.mock;

import domain.*;
import org.adorsys.envutils.EnvProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import spi.OnlineBankingService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(MockBanking.class);

    private String bearerToken;

    String mockConnectionUrl = null;

    public MockBanking(String bearerToken) {
        mockConnectionUrl = EnvProperties.getEnvOrSysProp("mockConnectionUrl", "http://localhost:10010");
        this.bearerToken = bearerToken;
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
        return StringUtils.endsWith(bankCode,"9999999");
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
    public Bank getBankLoginSettings(String bankCode) {
        return null;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, String pin, boolean storePin) {
        BankAccount[] bankAccounts = getRestTemplate().getForObject(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts", BankAccount[].class,bankCode);
        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.bankName(bankAccess.getBankName());
            bankAccount.externalId(bankApi(), UUID.randomUUID().toString());
        }
        return Arrays.asList(bankAccounts);
    }

    @Override
    public void removeBankAccount(BankAccount bankAccount, BankApiUser bankApiUser) {
              getRestTemplate().delete(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}",bankAccount.getBlz(),bankAccount.getIban());
    }

    @Override
    public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode, BankAccount bankAccount, String pin) {
        Booking[] bookings = getRestTemplate().getForObject(mockConnectionUrl + "/bankaccesses/{bankcode}/accounts/{iban}/bookings", Booking[].class, bankCode,bankAccount.getIban());
        return LoadBookingsResponse.builder()
                .bookings(Arrays.asList(bookings))
                .build();
    }

    private RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(bearerToken));
        return restTemplate;
    }
}
