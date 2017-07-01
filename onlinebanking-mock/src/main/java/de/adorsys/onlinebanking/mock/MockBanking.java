package de.adorsys.onlinebanking.mock;

import domain.*;
import org.adorsys.envutils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.web.client.RestTemplate;
import spi.OnlineBankingService;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(MockBanking.class);
    
    @Autowired
    private Principal principal;

    public enum Status {
        OK,
        SYNC,
        PIN,
        TAN,
        ERROR
    }

    String mockConnectionUrl = null;
    public MockBanking() {
    	mockConnectionUrl = EnvProperties.getEnvOrSysProp("mockConnectionUrl", "http://localhost:10010");
    }

    @Override
    public BankApi bankApiIdentifier() {
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
        return true;
    }

    @Override
    public BankApiUser registerUser(String uid) {
        //no registration needed
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
    	Authentication auth =  (Authentication) principal;
    	@SuppressWarnings("unchecked")
		Map<String, Object> credentials = (Map<String, Object>) auth.getCredentials();
    	String token = (String) credentials.get("bearerToken");    	
    	restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(token));
    	return restTemplate;
    }
}
