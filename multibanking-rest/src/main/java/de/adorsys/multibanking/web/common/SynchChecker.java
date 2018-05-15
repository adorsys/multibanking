package de.adorsys.multibanking.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.domain.BankAccountData;
import domain.BankAccount;

public class SynchChecker {
    private final static Logger LOGGER = LoggerFactory.getLogger(SynchChecker.class);

    /**
     * Checks the synchronization status of this bank account.
     * 
     * We distinguish following states:
     *  1- The account has never been synched.:
     *  	- If : synch status time is null
     *  	- Then: Last synch is null and synchStatus is null.
     *  	- Therefore : the synch can go on.
     *  2- A synch process has been started:
     *  	- If: Last synch status time is not null
     *  	- Then:
     *  		- If: SynchStatus is Ready? Then synch can go on.
     *  		- If: Synch status is synch or synch? Then 102
     *  3- We will later use the last synch status time to recover from failled synch.
     *   
     * @param accessId
     * @param accountId
     */
    public static void checkSynch(BankAccountData accountData){
    	// 1- Never synched
    	if(accountData.getSyncStatusTime()==null) return;
    	
    	// Ready: then go.
        if (accountData.getBankAccount().getSyncStatus() == BankAccount.SyncStatus.READY) return;
        
        // Pending: then 102
        if (accountData.getBankAccount().getSyncStatus() == BankAccount.SyncStatus.SYNC)
        	throw new SyncInProgressException(accountData.getBankAccount().getBankAccessId(), accountData.getBankAccount().getId());
        
        // TODO: When do we need the pending status
        if (accountData.getBankAccount().getSyncStatus() == BankAccount.SyncStatus.PENDING){
        	LOGGER.warn("The synch status pending is not expected in this application. We will allow synch to go on.");
        }
    }
}
