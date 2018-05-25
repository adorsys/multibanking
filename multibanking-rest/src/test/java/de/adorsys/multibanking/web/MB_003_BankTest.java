package de.adorsys.multibanking.web;

import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by peter on 09.05.18 at 09:55.
 */
public class MB_003_BankTest extends MB_BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_003_BankTest.class);
    final static String BANK_URI = "api/v1/bank";

    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

    @Test
    public void test_1() {
        try {
            URI uri = bankPath(this).build().toUri();
            this.setNextExpectedStatusCode(200);
            ResponseEntity<String> banks = this.testRestTemplate.getForEntity(uri, String.class);
            Assert.assertNotNull(banks);

            String foundBanksString = banks.getBody();
            String expectedBanksString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("mock_bank.json"));
            LOGGER.debug("expected banks:" + expectedBanksString);
            LOGGER.debug("found    banks:" + foundBanksString);
            JSONArray expectedBanks = new JSONArray(expectedBanksString);
            JSONArray foundBanks = new JSONArray(foundBanksString);
            Assert.assertEquals(expectedBanks.length(), foundBanks.length());
            for (int i = 0; i<foundBanks.length(); i++) {
                String fouindBankCode = foundBanks.getJSONObject(i).getString("bankCode");
                String expectedBankCode = expectedBanks.getJSONObject(i).getString("bankCode");
                Assert.assertEquals(expectedBankCode, fouindBankCode);
                LOGGER.debug("bankCode " + fouindBankCode + " ok");
            }
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    private static UriComponentsBuilder bankPath(MB_BaseTest base) {
        return base.path(BANK_URI);
    }

}
