package de.adorsys.onlinebanking.mock;

import domain.*;
import org.adorsys.envutils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import spi.OnlineBankingService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexg on 17.05.17.
 */
public class MockBanking implements OnlineBankingService {

    private static final Logger LOG = LoggerFactory.getLogger(MockBanking.class);

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
    public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String pin) {
    	RestTemplate restTemplate = new RestTemplate();
    	Map<String,String> map = new HashMap<>();
    	map.put("bankLogin", bankAccess.getBankLogin());
    	map.put("password", pin);
    	ResponseEntity<AccessToken> responseEntity = restTemplate.getForEntity(mockConnectionUrl + "/token/{bankLogin}/{password}", AccessToken.class, map);

    	restTemplate = new RestTemplate();
    	String bearerToken = responseEntity.getBody().getToken();
    	restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(bearerToken));
    	BankAccount[] bankAccounts = restTemplate.getForObject(mockConnectionUrl + "/accounts/", BankAccount[].class);

    	return Arrays.asList(bankAccounts);
    }

    @Override
    public List<Booking> loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, BankAccount bankAccount, String pin) {
    	RestTemplate restTemplate = new RestTemplate();
    	Map<String,String> map = new HashMap<>();
    	map.put("bankLogin", bankAccess.getBankLogin());
    	map.put("password", pin);
    	ResponseEntity<AccessToken> responseEntity = restTemplate.getForEntity(mockConnectionUrl + "/token/{bankLogin}/{password}", AccessToken.class, map);

    	restTemplate = new RestTemplate();
    	String bearerToken = responseEntity.getBody().getToken();
    	restTemplate.getInterceptors().add(new BearerTokenAuthorizationInterceptor(bearerToken));
//    	HashMap<Object,Object> hashMap = new HashMap<>();
//    	hashMap.put("accountId", bankAccount.getIbanHbciAccount());
    	Booking[] bookings = restTemplate.getForObject(mockConnectionUrl + "/accounts/{accountId}/bookings", Booking[].class, bankAccount.getIbanHbciAccount());

    	return Arrays.asList(bookings);
    }
}
