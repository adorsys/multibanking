package de.adorsys.multibanking.web;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import de.adorsys.multibanking.domain.UserData;
import domain.BankAccount.SyncStatus;

/**
 * https://wiki.adorsys.de/display/DOC/Multibanking-Rest+Tests
 */
@RunWith(SpringRunner.class)
public class MB_003b_UserData extends MB_BaseTest {

    @Before
    public void setup() throws Exception {
        super.setupBank();
    }

    @Test
    public void test_1() {
    	MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserData userData = MB_003a_UserData.loadUserData(this);
        
        Assume.assumeNotNull(userData);
        SyncStatus syncStatus = readSyncStatus(userData);
        // syncStatus should have a default value (SyncStatus.READY), not a null value.
        Assert.assertEquals(SyncStatus.READY, syncStatus);
    }

    private SyncStatus readSyncStatus(UserData userData) {
    	SyncStatus syncStatus = userData.getBankAccesses().iterator().next().getBankAccounts().iterator().next().getBankAccount().getSyncStatus();
    	return syncStatus;
	}
}
