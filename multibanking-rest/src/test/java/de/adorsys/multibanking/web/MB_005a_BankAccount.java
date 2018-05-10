package de.adorsys.multibanking.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.domain.UserData;

/**
 * Created by peter on 07.05.18 at 08:36.
 */

import de.adorsys.multibanking.web.account.BankAccountController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_005a_BankAccount extends MB_BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_005a_BankAccount.class);

    @Test
    // TODO läuft nicht wegen https://jira.adorsys.de/browse/MUL-272
    public void test_synch_bank_accounts_check_booking_files() {
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
            // Hello Peter, this synch is working. Then we must expect 204. 102 only happens when we
            // Send a ynch while anotherone is still working.
            this.setNextExpectedStatusCode(204);
            LOGGER.info("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, pin);
            
            List<BankAccountData> bankAccountData = getBankAccountData(this);
            bankAccountData.forEach(bad -> {
            	Map<String, BookingFile> bookingFiles = bad.getBookingFiles();
            	Assert.assertNotNull(bookingFiles);
            	Assert.assertFalse(bookingFiles.isEmpty());
            });
        });

    }

    public static UriComponentsBuilder bankAccountPath(MB_BaseTest base, BankAccessID accessID) {
        String basePath = BankAccountController.BASE_PATH;
        return base.path(basePath.replace("{accessId}", accessID.getValue()));
    }
    
    public static List<BankAccountData> getBankAccountData(MB_BaseTest base){
        UserData userData = MB_003a_UserData.loadUserData(base);
        BankAccessData bankAccessData = userData.getBankAccesses().get(0);
        return bankAccessData.getBankAccounts();
    }
}
