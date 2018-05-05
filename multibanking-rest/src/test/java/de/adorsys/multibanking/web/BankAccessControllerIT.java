package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.multibanking.web.account.BankAccessController;
import de.adorsys.multibanking.web.base.BaseControllerIT;
import de.adorsys.multibanking.web.base.PasswordGrantResponse;
import domain.BankAccess;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class BankAccessControllerIT extends BaseControllerIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(BankAccessControllerIT.class);
    @Autowired
    private BankService bankService;

    private String userId = Ids.uuid();
    private String password = Ids.uuid();
    private PasswordGrantResponse resp;

    @Before
    public void setup() throws Exception {
        resp = auth(userId, password);
        Assume.assumeFalse(StringUtils.isEmpty(resp.getAccessToken()));
        Assume.assumeTrue("Bearer".equals(resp.getTokenType()));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mock_bank.json");
        bankService.importBanks(inputStream);
        Optional<BankEntity> bankEntity = bankService.findByBankCode("19999999");
        Assume.assumeTrue(bankEntity.isPresent());
    }

    @Test
    public void id1_testCreateBankaccess() throws Exception {
        createBankAccess("19999999", "m.becker", "12345");
    }

    @Test
    public void id2_testCreateAndDeleteBankaccess() throws Exception {
        String accessID = createBankAccess("19999999", "m.becker", "12345");

        URI deleteUri = bankAccessPath().pathSegment(accessID).build().toUri();
        LOGGER.info("deleteURI:" + deleteUri.toString());

        setNextExpectedStatusCode(204);
        LOGGER.info("----------------------- first delete -------------------------");
        testRestTemplate.delete(deleteUri);

        setNextExpectedStatusCode(410);
        LOGGER.info("----------------------- second delete -------------------------");
        testRestTemplate.delete(deleteUri);
    }

    private UriComponentsBuilder bankAccessPath() {
        return path(BankAccessController.BASE_PATH);
    }

    private String createBankAccess(String bankCode, String bankLogin, String pin) {
        try {
            BankAccessEntity be = new BankAccessEntity();
            be.setBankCode(bankCode);
            be.setBankLogin(bankLogin);
            be.setPin(pin);
            URI uri = bankAccessPath().build().toUri();
            setNextExpectedStatusCode(201);
            URI location = testRestTemplate.postForLocation(uri, be);
            Assert.assertNotNull(location);
            setNextExpectedStatusCode(200);
            String userData = testRestTemplate.getForObject(location, String.class);
            String accessID = null;
            {
                Assert.assertNotNull(userData);
                LOGGER.info("user data\n" + userData);
                JSONObject j = new JSONObject(userData);
                JSONObject userEntity = j.getJSONObject("userEntity");
                String userId = userEntity.getString("id");
                LOGGER.info("user id:" + userId);

                JSONArray bankAccesses = j.getJSONArray("bankAccesses");
                Assert.assertEquals(1, bankAccesses.length());
                JSONObject bankAccessWrapper = bankAccesses.getJSONObject(0);
                JSONObject bankAccess = bankAccessWrapper.getJSONObject("bankAccess");
                Assert.assertEquals(bankAccess.getString("userId"), userId);
                String bankLogin2 = bankAccess.getString("bankLogin");
                LOGGER.info("bankLogin:" + bankLogin);
                Assert.assertEquals(bankLogin, bankLogin2);
                accessID = bankAccess.getString("id");
            }
            return accessID;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
