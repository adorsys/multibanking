package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.web.account.BankAccessController;
import de.adorsys.multibanking.web.base.BankLoginTuple;
import de.adorsys.multibanking.web.base.BaseControllerIT;
import de.adorsys.multibanking.web.base.UserPasswordTuple;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.encobject.service.api.ExtendedStoreConnection;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_004_BankAccess extends BaseControllerIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(MB_004_BankAccess.class);
    @Autowired
    private BankService bankService;
    private UserPasswordTuple userPasswordTuple;
    private ExtendedStoreConnection c = ExtendedStoreConnectionFactory.get();

    @Before
    public void setup() throws Exception {
        c.listAllBuckets().forEach(bucket -> c.deleteContainer(bucket));
//        LOGGER.info("check filsystem ####################################################");
//        Thread.currentThread().sleep(5000);

        userPasswordTuple = new UserPasswordTuple("peter-" + UUID.randomUUID(), "allwaysTheSamePassword");
        auth(userPasswordTuple);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mock_bank.json");
        bankService.importBanks(inputStream);
        Optional<BankEntity> bankEntity = bankService.findByBankCode("19999999");
        Assume.assumeTrue(bankEntity.isPresent());
    }

    @Test
    public void test_1() {
        createBankAccess(new BankLoginTuple("19999999", "m.becker", "12345"));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test_2() {
        List<BankAccessID> bankAccessIDs = loadBankAccess(createBankAccess(new BankLoginTuple("19999999", "m.becker", "12345")));
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath().pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

        setNextExpectedStatusCode(204);
        testRestTemplate.delete(deleteUri);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void test_3() {
        List<BankAccessID> bankAccessIDs = loadBankAccess(createBankAccess(new BankLoginTuple("19999999", "m.becker", "12345")));
        Assert.assertEquals(1, bankAccessIDs.size());

        URI deleteUri = bankAccessPath().pathSegment(bankAccessIDs.get(0).getValue()).build().toUri();

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
            location = createBankAccess(new BankLoginTuple("19999999", "m.becker", "12345"));
        }

        List<BankAccessID> bankAccessIDs = loadBankAccess(location);
        SortedSet<String> uniqueSet = new TreeSet<>();
        bankAccessIDs.forEach(bankAccessID -> uniqueSet.add(bankAccessID.getValue()));
        Assert.assertEquals(max, uniqueSet.size());

        Assert.assertEquals(max, bankAccessIDs.size());
        for (int i = 0; i<max; i++) {
            URI deleteUri = bankAccessPath().pathSegment(bankAccessIDs.get(i).getValue()).build().toUri();
            setNextExpectedStatusCode(204);
            testRestTemplate.delete(deleteUri);
        }
    }

    private UriComponentsBuilder bankAccessPath() {
        return path(BankAccessController.BASE_PATH);
    }

    private URI createBankAccess(BankLoginTuple bankLogin) {
        try {
            BankAccessEntity be = new BankAccessEntity();
            be.setBankCode(bankLogin.getBankCode());
            be.setBankLogin(bankLogin.getUserID());
            be.setPin(bankLogin.getUserPIN());
            URI uri = bankAccessPath().build().toUri();
            setNextExpectedStatusCode(201);
            URI location = testRestTemplate.postForLocation(uri, be);
            Assert.assertNotNull(location);
            setNextExpectedStatusCode(200);
            return location;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private List<BankAccessID> loadBankAccess(URI location) {
        try {
            List<BankAccessID> list = new ArrayList<>();
            String userData = testRestTemplate.getForObject(location, String.class);
            Assert.assertNotNull(userData);
            LOGGER.info("user data\n" + userData);
            JSONObject j = new JSONObject(userData);
            JSONObject userEntity = j.getJSONObject("userEntity");
            String userId = userEntity.getString("id");
            LOGGER.info("user id:" + userId);

            JSONArray bankAccesses = j.getJSONArray("bankAccesses");
            for (int i = 0; i < bankAccesses.length(); i++) {
                JSONObject bankAccessWrapper = bankAccesses.getJSONObject(i);
                JSONObject bankAccess = bankAccessWrapper.getJSONObject("bankAccess");
                Assert.assertEquals(bankAccess.getString("userId"), userId);
                String bankLogin2 = bankAccess.getString("bankLogin");
                list.add(new BankAccessID(bankAccess.getString("id")));

            }
            return list;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

}
