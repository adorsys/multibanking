package de.adorsys.onlinebanking.mock;

import java.util.Arrays;
import java.util.List;

import domain.*;
import org.adorsys.envutils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import spi.OnlineBankingService;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(MockBanking.class);
    
    private String bearerToken;

    public enum Status {
        OK,
        SYNC,
        PIN,
        TAN,
        ERROR
    }

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
    public BankLoginSettings getBankLoginSettings(String bankCode) {
        return null;
    }

    @Override
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin, boolean storePin) {
    	BankAccount[] bankAccounts = getRestTemplate().getForObject(mockConnectionUrl + "/accounts/", BankAccount[].class);

    	return Arrays.asList(bankAccounts);
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin) {
    	Booking[] bookings = getRestTemplate().getForObject(mockConnectionUrl + "/accounts/{accountId}/bookings", Booking[].class, bankAccount.getIban());

    	return Arrays.asList(bookings);
    }
    
    private RestTemplate getRestTemplate(){
    	RestTemplate restTemplate = new RestTemplate();
    	restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(bearerToken));
    	return restTemplate;
    }
}
