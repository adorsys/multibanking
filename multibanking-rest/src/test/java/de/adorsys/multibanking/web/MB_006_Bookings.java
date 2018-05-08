package de.adorsys.multibanking.web;

import de.adorsys.multibanking.web.account.BookingController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Created by peter on 08.05.18 at 15:49.
 */
public class MB_006_Bookings extends MB_BaseTest {
    // @Test
    // TODO läuft nicht wegen https://jira.adorsys.de/browse/MUL-272

    public void test_1() {
        URI location = MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccess.loadBankAccess(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        bankAccountIDs.forEach(bankAccountID -> {
            LOGGER.info("found bank-AccountID:" + bankAccountID.toString());
            URI uri = bankAccountPath(this, bankAccessIDs.get(0), bankAccountID).queryParam("period","2018").build().toUri();
            this.setNextExpectedStatusCode(200);
            LOGGER.info("GET TO uri:" + uri);
            ResponseEntity<String> bookings = this.testRestTemplate.getForEntity(uri, String.class);
            LOGGER.info("bookings:" + bookings);
        });

    }

    private static UriComponentsBuilder bankAccountPath(MB_BaseTest base, BankAccessID accessID, BankAccountID accountID) {
        String path = BookingController.BASE_PATH;
        path = path.replace("{accessId}", accessID.getValue());
        path = path.replace("{accountId}", accountID.getValue());
        return base.path(path);
    }

}
