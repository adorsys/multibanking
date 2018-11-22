package de.adorsys.multibanking.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BankAccessCredentials;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidPinException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.producer.OnlineBankingServiceProducer;
import de.adorsys.multibanking.utils.FQNUtils;
import de.adorsys.multibanking.utils.Ids;
import domain.BankApiUser;
import spi.OnlineBankingService;

/**
 * A user can have 0 to N bank accesses.
 *
 * @author fpo 2018-04-06 05:47
 *
 */
@Service
public class BankAccessService  {
	@Autowired
	private UserObjectService uos;

	@Autowired
	private BankAccessCredentialService credentialService;

	@Autowired
    private UserDataService uds;

	@Autowired
    private OnlineBankingServiceProducer bankingServiceProducer;

    @Autowired
    private BankAccountService bankAccountService;


    /**
     * Create a bank access
     * - load and store bank accounts
     *
     * @param bankAccess
     * @return
     */
    public BankAccessEntity createBankAccess(BankAccessEntity bankAccess) {
    	// Set user and access id
    	bankAccess.setUserId(uos.auth().getUserID().getValue());
    	// Set an accessId if none.
    	if(StringUtils.isBlank(bankAccess.getId())){
    		bankAccess.setId(Ids.uuid());
    	} else {
    		// Check bank Access with Id does not exists.
    		if(exists(bankAccess.getId())){
    			throw new BankAccessAlreadyExistException(bankAccess.getId());
    		}
    	}

    	BankAccessCredentials credentials = bankAccess.cloneCredentials();
		bankAccess.cleanCredentials();

		// disect credentials
        if (bankAccess.isStorePin()) {
        	credentialService.store(credentials);
        }

        // store bank access
    	storeBankAccess(bankAccess);

    	try {
	    	// pull and store bank accounts
	    	bankAccountService.synchBankAccounts(bankAccess, credentials);
    	} catch (InvalidPinException e){
    		// Set pin valid state to false.
            if (bankAccess.isStorePin()) {
            	credentialService.invalidate(credentials);
            }
            throw e;
    	}

    	return bankAccess;
    }

    /**
     * Update the bank access object.
     * Credentials are reset but not updated. USe another interface for managing credentials.
     *
     * @param bankAccessEntity
     */
	public void updateBankAccess(BankAccessEntity bankAccessEntity) {
		storeBankAccess(bankAccessEntity);
    }

    public boolean deleteBankAccess(String accessId) {
    	return deleteBankAccessInternal(accessId, uds.load());
    }

	/**
	 * Check existence by checking if the file containing the list of bank accounts exits.
	 *
	 * @param accessId
	 * @return
	 */
	public boolean exists(String accessId){
		UserData userData = uds.load();
		return userData.containsKey(accessId);
	}

    /*
     * Clean bank access credential before storage.
     * @param bankAccess
     */
	private void storeBankAccess(BankAccessEntity bankAccess) {
		bankAccess.cleanCredentials();
		UserData userData;
		if(!uds.exists()){
			userData = uds.createUser(null);
		} else {
			userData = uds.load();
		}
		BankAccessData accessData = userData.getBankAccess(bankAccess.getId())
				.orElseGet(() -> {
					BankAccessData b = new BankAccessData();
					userData.put(bankAccess.getId(), b);
					return b;
				});

		accessData.setBankAccess(bankAccess);
		uds.store(userData);
	}

    private boolean deleteBankAccessInternal(String accessId, UserData userData) {
    	BankAccessData accessData = userData.remove(accessId);
		if(accessData!=null){
			uds.store(userData);
			uos.flush();
			// TODO: for transactionality. Still check existence of these files.
	    	removeRemoteRegistrations(accessData, userData);
	    	uos.deleteDirectory(FQNUtils.bankAccessDirFQN(accessId));
//	    	uos.clearCached(FQNUtils.bankAccessDirFQN(accessId));
	    	return true;
		}
		return false;
    }
    
	private void removeRemoteRegistrations(BankAccessData accessData, UserData userData) {
    	// Load bank Accounts
		List<BankAccountData> bankAccountDataList = accessData.getBankAccounts();
        UserEntity userEntity = userData.getUserEntity();

		bankAccountDataList.stream().forEach(bankAccountData -> {
			BankAccountEntity bankAccountEntity = bankAccountData.getBankAccount();
		   	bankAccountEntity.getExternalIdMap().keySet().forEach(bankApi -> {
	   			OnlineBankingService bankingService = bankingServiceProducer.getBankingService(bankApi);
	   			//remove remote bank api user
	   			if (bankingService.userRegistrationRequired()) {
	   				BankApiUser bankApiUser = userEntity.getApiUser()
	   						.stream()
	   						.filter(apiUser -> apiUser.getBankApi() == bankApi)
	   						.findFirst()
	   						.orElseThrow(() -> new ResourceNotFoundException(BankApiUser.class, bankApi.toString()));
	   				bankingService.removeBankAccount(null, bankAccountEntity, bankApiUser);
	   			}
	   		});
	   	});
	}
}
