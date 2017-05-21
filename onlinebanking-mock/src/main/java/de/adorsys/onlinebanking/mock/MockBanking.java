package de.adorsys.onlinebanking.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adorsys.envutils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import domain.AccessToken;
import domain.BankAccess;
import domain.BankAccount;
import domain.BankApi;
import domain.BankApiUser;
import domain.Booking;
import spi.OnlineBankingService;

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
