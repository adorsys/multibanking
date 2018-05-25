package de.adorsys.multibanking.web;

import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import de.adorsys.multibanking.web.base.entity.UserDataStructure;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Created by peter on 07.05.18 at 08:36.
 */

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_005_BankAccountTest extends MB_BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_005_BankAccountTest.class);
    public static final String SYNC_URI = "/api/v1//bankaccesses/{accessId}/accounts/{accountId}/sync";


    @Test
    public void test_1_synch_bank_account_returns_204() {
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = accessDataWith2BankAccounts(this, location);
        BankAccessID bankAccessID = userDataStructure.getBankAccessIDs().get(0);
        userDataStructure.getBankAccountIDs(bankAccessID).forEach(bankAccountID ->
                syncBankAccount204(this, bankAccessID, bankAccountID)
        );
    }

    @Test
    public void synched_bank_account_status_ready_unsynched_bank_account_status_null() {
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = accessDataWith2BankAccounts(this, location);
        BankAccessID firstBankAccessID = userDataStructure.getBankAccessIDs().get(0);
        BankAccountID firstBankAccountID = userDataStructure.getBankAccountIDs(firstBankAccessID).get(0);
        // Check synch data0 are still null
        Assert.assertFalse(userDataStructure.getSyncStatus(firstBankAccessID, firstBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getLastSync(firstBankAccessID, firstBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getSyncStatusTime(firstBankAccessID, firstBankAccountID).isPresent());
        // Sync first bank account
        syncBankAccount204(this, firstBankAccessID, firstBankAccountID);

        // Check second bankAccount data are still null
        BankAccountID secondBankAccountID = userDataStructure.getBankAccountIDs(firstBankAccessID).get(1);
        Assert.assertFalse(userDataStructure.getSyncStatus(firstBankAccessID, secondBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getLastSync(firstBankAccessID, secondBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getSyncStatusTime(firstBankAccessID, secondBankAccountID).isPresent());

        // Do not synch second bank account.
        // Reload account data
        userDataStructure = accessDataWith2BankAccounts(this, location);
        LOGGER.debug("nach dem sync " + userDataStructure.toString());
        // Assert synch data0 set
        Assert.assertEquals("READY", userDataStructure.getSyncStatus(firstBankAccessID, firstBankAccountID).get().getValue());
        Assert.assertTrue(userDataStructure.getLastSync(firstBankAccessID, firstBankAccountID).isPresent());
        Assert.assertTrue(userDataStructure.getSyncStatusTime(firstBankAccessID, firstBankAccountID).isPresent());

        // Check second bankAccount data are still null
        Assert.assertFalse(userDataStructure.getSyncStatus(firstBankAccessID, secondBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getLastSync(firstBankAccessID, secondBankAccountID).isPresent());
        Assert.assertFalse(userDataStructure.getSyncStatusTime(firstBankAccessID, secondBankAccountID).isPresent());
    }

    @Test
    public void test_synch_bank_accounts_check_booking_files() {
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccessTest.loadUserDataStructure(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        BankAccessID firstBankAccessID = bankAccessIDs.get(0);
        userDataStructure.getBankAccountIDs(firstBankAccessID).forEach(bankAccountID -> {
            LOGGER.debug("found bank-AccountID:" + bankAccountID.toString());
            URI uri = syncPath(this, bankAccessIDs.get(0), bankAccountID);
            // Hello Peter, this synch is working. Then we must expect 204. 102 only happens when we
            // Send a snch while anotherone is still working.
            this.setNextExpectedStatusCode(204);
            LOGGER.debug("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, PIN);
        });

        UserDataStructure reloadedUserDataStructure = MB_004_BankAccessTest.loadUserDataStructure(this, location);
        userDataStructure.getBankAccountIDs(firstBankAccessID).forEach(bankAccountID -> {
            Optional<List<String>> bookingPeriods = reloadedUserDataStructure.getBookingPeriods(firstBankAccessID, bankAccountID);
            Assert.assertTrue(bookingPeriods.isPresent());
            Assert.assertFalse(bookingPeriods.get().isEmpty());
        });
    }

    @Test
    public void test_synch_bank_accounts_wrong_pin_shall_return_403() {
        URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccessTest.loadUserDataStructure(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        bankAccountIDs.forEach(bankAccountID -> {
            URI uri = syncPath(this, bankAccessIDs.get(0),bankAccountID);
            this.setNextExpectedStatusCode(403);
            LOGGER.debug("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, WRONG_PIN);
        });
    }
    
    public static UserDataStructure accessDataWith2BankAccounts(MB_BaseTest base, URI location) {
        UserDataStructure userDataStructure = MB_004_BankAccessTest.loadUserDataStructure(base, location);
        Assert.assertEquals(1, userDataStructure.getBankAccessIDs().size());
        BankAccessID bankAccessID = userDataStructure.getBankAccessIDs().get(0);
        Assert.assertEquals(2, userDataStructure.getBankAccountIDs(bankAccessID).size());
        return userDataStructure;
    }

    public static void syncBankAccount204(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID) {
        LOGGER.debug("found bank-AccountID:" + bankAccountID.toString());
        URI uri = syncPath(base, accessID, bankAccountID);
        base.setNextExpectedStatusCode(204);
        LOGGER.debug("PUT TO uri:" + uri);
        base.testRestTemplate.put(uri, base.PIN);
    }

    public static URI syncPath(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID) {
        return base.path(SYNC_URI).build(accessID.getValue(), bankAccountID.getValue());
    }
}
