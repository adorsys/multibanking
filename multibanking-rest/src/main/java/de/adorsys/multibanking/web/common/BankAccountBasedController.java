package de.adorsys.multibanking.web.common;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.service.BankAccountService;
import domain.BankAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BankAccountBasedController extends BankAccessBasedController {
    private final static Logger LOGGER = LoggerFactory.getLogger(BankAccountBasedController.class);

    @Autowired
    protected BankAccountService bankAccountService;

    protected void checkBankAccountExists(String accessId, String accountId){
    	checkBankAccessExists(accessId);
        if (!bankAccountService.exists(accessId, accountId)) 
            throw new ResourceNotFoundException(BankAccountEntity.class, accessId + ":" +accountId);
    }
    
    protected void checkSynch(String accessId, String accountId){
        BankAccount.SyncStatus syncStatus = bankAccountService.getSyncStatus(accessId, accountId);
        if (syncStatus != BankAccount.SyncStatus.READY) {
            LOGGER.info("Syncstatus für " + accessId + " " + accountId + " ist " + syncStatus);
            throw new SyncInProgressException(accessId, accountId);
        }
    }
}
