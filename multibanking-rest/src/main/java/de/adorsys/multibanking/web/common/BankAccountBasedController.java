package de.adorsys.multibanking.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.service.BankAccountService;
import domain.BankAccount;

public abstract class BankAccountBasedController extends BankAccessBasedController {
    private final static Logger LOGGER = LoggerFactory.getLogger(BankAccountBasedController.class);

    @Autowired
    protected BankAccountService bankAccountService;

    protected void checkBankAccountExists(String accessId, String accountId){
    	checkBankAccessExists(accessId);
        if (!bankAccountService.exists(accessId, accountId)) 
            throw new ResourceNotFoundException(BankAccountEntity.class, accessId + ":" +accountId);
    }
    
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
    protected void checkSynch(String accessId, String accountId){
    	BankAccountData accountData = bankAccountService.loadBankAccount(accessId, accountId);
    	SynchChecker.checkSynch(accountData);
    }
}
