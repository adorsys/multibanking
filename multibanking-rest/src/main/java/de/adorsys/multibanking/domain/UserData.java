package de.adorsys.multibanking.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.adorsys.multibanking.exception.ResourceNotFoundException;
import lombok.Data;

/**
 * This is the user data object. It is the unit of storage.
 * 
 * @author fpo 2018-04-02 03:55
 *
 */
@Data
public class UserData {
	private UserEntity userEntity;
	private Map<String, BankAccessData> bankAccesses = new HashMap<>();
	private Map<String, ContractEntity> contracts = new HashMap<>();
	
	private AccountSynchPref accountSynchPref;

	public Optional<BankAccessData> getBankAccess(String accessId) {
		return Optional.ofNullable(bankAccesses.get(accessId));
	}
	
	public BankAccessData bankAccessData(String accessId){
		return getBankAccess(accessId).orElseThrow(() -> new ResourceNotFoundException(BankAccessData.class, accessId));
	} 
	public BankAccountData bankAccountData(String accessId, String accountId){
		return bankAccessData(accessId)
				.getBankAccount(accountId).orElseThrow(() -> new ResourceNotFoundException(BankAccountData.class, accessId));
	} 
	
}
