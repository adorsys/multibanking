package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccessStructure;
import de.adorsys.multibanking.web.base.entity.UserDataStructure;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_004_BankAccessTest extends MB_BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MB_004_BankAccessTest.class);
    public static final String ACCESS_URI = "/api/v1/bankaccesses";


    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

    @Test
    public void test_1() {
        createBankAccess(this, theBeckerTuple);
    }

    @Test
    public void test_2() {
        List<BankAccessID> bankAccessIDs = loadUserDataStructure(this, createBankAccess(this, theBeckerTuple)).getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath(this).pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

        setNextExpectedStatusCode(204);
        testRestTemplate.delete(deleteUri);
    }

    @Test
    public void test_3() {
        List<BankAccessID> bankAccessIDs = loadUserDataStructure(this, createBankAccess(this, theBeckerTuple)).getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath(this).pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

        setNextExpectedStatusCode(204);
        testRestTemplate.delete(deleteUri);

        setNextExpectedStatusCode(410);
        testRestTemplate.delete(deleteUri);
    }

    @Test
    public void test_4() {
        int max = 5;
        URI location = null;
        for (int i = 0; i<max; i++) {
            location = createBankAccess(this, theBeckerTuple);
        }

        List<BankAccessID> bankAccessIDs = loadUserDataStructure(this, location).getBankAccessIDs();
        SortedSet<String> uniqueSet = new TreeSet<>();
        bankAccessIDs.forEach(bankAccessID -> uniqueSet.add(bankAccessID.getValue()));
        Assert.assertEquals(max, uniqueSet.size());

        Assert.assertEquals(max, bankAccessIDs.size());
        for (int i = 0; i<max; i++) {
            URI deleteUri = bankAccessPath(this).pathSegment(bankAccessIDs.get(i).getValue()).build().toUri();
            setNextExpectedStatusCode(204);
            testRestTemplate.delete(deleteUri);
        }
    }

    public static URI createBankAccess(MB_BaseTest base, BankAccessStructure bankAccessStructure) {
        try {
            URI uri = bankAccessPath(base).build().toUri();
            base.setNextExpectedStatusCode(201);
            URI location = base.testRestTemplate.postForLocation(uri, bankAccessStructure);
            Assert.assertNotNull(location);
            return location;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static UserDataStructure loadUserDataStructure(MB_BaseTest base, URI location) {
        try {
            base.setNextExpectedStatusCode(200);
            String userData = base.testRestTemplate.getForObject(location, String.class);
            Assert.assertNotNull(userData);
            JSONObject j = new JSONObject(userData);
            UserDataStructure userDataStructure = new UserDataStructure(j);
            LOGGER.debug(userDataStructure.toString());
            return userDataStructure;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Test
    public void user_data_not_null_on_create_bank_access() {
        UserDataStructure userDataStructure = loadUserDataStructure(this, MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple));
        Assert.assertNotNull(userDataStructure);
    }
    
    @Test
    public void forbiden_on_create_bank_access_wrong_pin() {
    	BankAccessStructure bankLogin = new BankAccessStructure("19999999", "m.becker", WRONG_PIN);
        BankAccessEntity be = new BankAccessEntity();
        be.setBankCode(bankLogin.getBankCode());
        be.setBankLogin(bankLogin.getBankLogin());
        be.setPin(bankLogin.getPin());
        URI uri = bankAccessPath(this).build().toUri();
        this.setNextExpectedStatusCode(403);
        this.testRestTemplate.postForLocation(uri, be);
    }

    /**
     * After loading the bank account account, the synch status is supposed to be null. This is the initial state.
     */
    @Test
    public void bank_account_synch_status_null_on_create_bank_access() {
        UserDataStructure userDataStructure = loadUserDataStructure(this, MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple));
        Assume.assumeNotNull(userDataStructure);
        userDataStructure.getBankAccessIDs().forEach(bankAccessID -> {
            userDataStructure.getBankAccountIDs(bankAccessID).forEach(bankAccountID -> {
                Assert.assertFalse(userDataStructure.getSyncStatus(bankAccessID, bankAccountID).isPresent());
            });
        });
    }

    /**
     * After loading the bank account account, the last synch property is supposed to be null. This is the initial state.
     */
    @Test
    public void bank_account_last_synch_null_on_create_bank_access() {
        UserDataStructure userDataStructure = loadUserDataStructure(this, MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple));
        Assume.assumeNotNull(userDataStructure);
        userDataStructure.getBankAccessIDs().forEach(bankAccessID -> {
            userDataStructure.getBankAccountIDs(bankAccessID).forEach(bankAccountID -> {
                Assert.assertFalse(userDataStructure.getLastSync(bankAccessID, bankAccountID).isPresent());
            });
        });
    }


    private static UriComponentsBuilder bankAccessPath(MB_BaseTest base) {
        return base.path(ACCESS_URI);
    }


}
