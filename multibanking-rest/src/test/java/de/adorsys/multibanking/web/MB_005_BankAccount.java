package de.adorsys.multibanking.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.domain.UserData;

/**
 * Created by peter on 07.05.18 at 08:36.
 */

import de.adorsys.multibanking.web.account.BankAccountController;
import de.adorsys.multibanking.web.base.BankLoginTuple;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import domain.BankAccount.SyncStatus;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_005_BankAccount extends MB_BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(MB_005_BankAccount.class);

    @Test
    public void synch_bank_account_returns_204() {
    	BankAccessData accessData = createAccessDataWith2BankAccounts(this);
    	BankAccessID bankAccessID = new BankAccessID(accessData.getBankAccess().getId());
    	accessData.getBankAccounts().forEach(accountData -> {
    		BankAccountID bankAccountID = new BankAccountID(accountData.getBankAccount().getId());
    		syncBankAccount204(this, bankAccessID, bankAccountID);
    	});
    }

    @Test
    public void synched_bank_account_status_ready_unsynched_bank_account_status_null() {
    	BankAccessData accessData = createAccessDataWith2BankAccounts(this);
    	BankAccessID bankAccessID = new BankAccessID(accessData.getBankAccess().getId());
    	// Check synch data0 are still null
    	BankAccountData accountData0 = accessData.getBankAccounts().get(0);
		BankAccountID bankAccountID0 = new BankAccountID(accountData0.getBankAccount().getId());
		Assume.assumeTrue(accountData0.getBankAccount().getSyncStatus()==null);
		Assume.assumeTrue(accountData0.getBankAccount().getLastSync()==null);
		Assume.assumeTrue(accountData0.getSynchStatusTime()==null);
    	// Sync first bank account
		syncBankAccount204(this, bankAccessID, bankAccountID0);

		// Check synch data0 are still null
    	BankAccountData accountData1 = accessData.getBankAccounts().get(1);
		Assume.assumeTrue(accountData1.getBankAccount().getSyncStatus()==null);
		Assume.assumeTrue(accountData1.getBankAccount().getLastSync()==null);
		Assume.assumeTrue(accountData1.getSynchStatusTime()==null);

		// Do not synch second bank account.
		// Reload account data
		accessData = loadAccessDataWith2BankAccounts(this);
		// Assert synch data0 set
    	accountData0 = accessData.getBankAccounts().get(0);
		Assert.assertEquals(SyncStatus.READY, accountData0.getBankAccount().getSyncStatus());
		Assert.assertNotNull(accountData0.getBankAccount().getLastSync());
		Assert.assertNotNull(accountData0.getSynchStatusTime());

    	accountData1 = accessData.getBankAccounts().get(1);
		Assert.assertNull(accountData1.getBankAccount().getSyncStatus());
		Assert.assertNull(accountData1.getBankAccount().getLastSync());
		Assert.assertNull(accountData1.getSynchStatusTime());
    }

    @Test
    public void test_synch_bank_accounts_check_booking_files() {
        URI location = MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccess.loadBankAccess(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        bankAccountIDs.forEach(bankAccountID -> {
            LOGGER.info("found bank-AccountID:" + bankAccountID.toString());
            URI uri = syncPath(this, bankAccessIDs.get(0),bankAccountID);
            String pin = null;
            // Hello Peter, this synch is working. Then we must expect 204. 102 only happens when we
            // Send a ynch while anotherone is still working.
            this.setNextExpectedStatusCode(204);
            LOGGER.info("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, pin);
            
            List<BankAccountData> bankAccountData = loadAccessDataWith2BankAccounts(this).getBankAccounts();
            bankAccountData.forEach(bad -> {
            	Map<String, BookingFile> bookingFiles = bad.getBookingFiles();
            	Assert.assertNotNull(bookingFiles);
            	Assert.assertFalse(bookingFiles.isEmpty());
            });
        });
    }
    
    @Test
    public void test_synch_bank_accounts_wrong_pin_shall_return_403() {
        URI location = MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccess.loadBankAccess(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        bankAccountIDs.forEach(bankAccountID -> {
            URI uri = syncPath(this, bankAccessIDs.get(0),bankAccountID);
            String pin = "1234567";
            this.setNextExpectedStatusCode(403);
            LOGGER.info("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, pin);
        });
    }
    

    public static URI syncPath(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID) {
    	return base.path( BankAccountController.SYNC_PATH).build(accessID.getValue(), bankAccountID.getValue());
    }
    
    public static BankAccessData createAccessDataWith2BankAccounts(MB_BaseTest base){
        MB_004_BankAccess.createBankAccess(base, base.theBeckerTuple);
        return loadAccessDataWith2BankAccounts(base);
    }
    public static BankAccessData loadAccessDataWith2BankAccounts(MB_BaseTest base){
        UserData userData = loadUserData(base);
        Assume.assumeNotNull(userData.getBankAccesses());
        Assume.assumeTrue(1==userData.getBankAccesses().size());
    	BankAccessData accessData = userData.getBankAccesses().get(0);
    	Assume.assumeTrue(2==accessData.getBankAccounts().size());
    	return accessData;
    }
    
    public static void syncBankAccount204(MB_BaseTest base, BankAccessID accessID, BankAccountID bankAccountID){
		LOGGER.info("found bank-AccountID:" + bankAccountID.toString());
		URI uri = syncPath(base, accessID,bankAccountID);
		base.setNextExpectedStatusCode(204);
		LOGGER.info("PUT TO uri:" + uri);
		String pin = null;
		base.testRestTemplate.put(uri, pin );
    }
}
