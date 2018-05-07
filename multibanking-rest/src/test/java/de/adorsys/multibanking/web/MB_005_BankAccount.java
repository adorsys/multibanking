package de.adorsys.multibanking.web;

/**
 * Created by peter on 07.05.18 at 08:36.
 */

import de.adorsys.multibanking.web.account.BankAccountController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_005_BankAccount extends MB_BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_005_BankAccount.class);

    @Test
    public void test_1() {

        URI location = MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccess.loadBankAccess(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        bankAccountIDs.forEach(bankAccountID -> {
            LOGGER.info("found bank-AccountID:" + bankAccountID.toString());
            URI uri = bankAccountPath(this, bankAccessIDs.get(0)).pathSegment(bankAccountID.getValue()).pathSegment("sync").build().toUri();
            String pin = null;
//            this.setNextExpectedStatusCode(102);
            LOGGER.info("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, pin);
        });

    }

    private static UriComponentsBuilder bankAccountPath(MB_BaseTest base, BankAccessID accessID) {
        String basePath = BankAccountController.BASE_PATH;
        return base.path(basePath.replace("{accessId}", accessID.getValue()));
    }

}
