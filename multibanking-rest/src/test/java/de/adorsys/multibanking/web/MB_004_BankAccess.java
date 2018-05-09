package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.web.account.BankAccessController;
import de.adorsys.multibanking.web.base.BankLoginTuple;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.junit.Assert;
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
public class MB_004_BankAccess extends MB_BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MB_004_BankAccess.class);


    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

    @Test
    public void test_1() {
        createBankAccess(this, theBeckerTuple);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test_2() {
        List<BankAccessID> bankAccessIDs = loadBankAccess(this, createBankAccess(this, theBeckerTuple)).getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath(this).pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

        setNextExpectedStatusCode(204);
        testRestTemplate.delete(deleteUri);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test_3() {
        List<BankAccessID> bankAccessIDs = loadBankAccess(this, createBankAccess(this, theBeckerTuple)).getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath(this).pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

        setNextExpectedStatusCode(204);
        testRestTemplate.delete(deleteUri);

        setNextExpectedStatusCode(410);
        testRestTemplate.delete(deleteUri);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test_4() {
        int max = 5;
        URI location = null;
        for (int i = 0; i<max; i++) {
            location = createBankAccess(this, theBeckerTuple);
        }

        List<BankAccessID> bankAccessIDs = loadBankAccess(this, location).getBankAccessIDs();
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

    public static URI createBankAccess(MB_BaseTest base, BankLoginTuple bankLogin) {
        try {
            BankAccessEntity be = new BankAccessEntity();
            be.setBankCode(bankLogin.getBankCode());
            be.setBankLogin(bankLogin.getUserID());
            be.setPin(bankLogin.getUserPIN());
            URI uri = bankAccessPath(base).build().toUri();
            base.setNextExpectedStatusCode(201);
            URI location = base.testRestTemplate.postForLocation(uri, be);
            Assert.assertNotNull(location);
            return location;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    public static UserDataStructure loadBankAccess(MB_BaseTest base, URI location) {
        try {
            base.setNextExpectedStatusCode(200);
            String userData = base.testRestTemplate.getForObject(location, String.class);
            Assert.assertNotNull(userData);
            LOGGER.info("user data\n" + userData);
            JSONObject j = new JSONObject(userData);
            return new UserDataStructure(j);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static UriComponentsBuilder bankAccessPath(MB_BaseTest base) {
        return base.path(BankAccessController.BASE_PATH);
    }


}