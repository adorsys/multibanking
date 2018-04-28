package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

/**
 * Holds Data associated with a bank access.
 * 
 * @author fpo
 *
 */
@Data
public class BankAccessData {

	private BankAccessEntity bankAccess;
	
	private Map<String, BankAccountData> bankAccounts = new HashMap<>();

	private AccountSynchPref accountSynchPref;

	public Optional<BankAccountData> getBankAccount(String accountId) {
		return Optional.ofNullable(bankAccounts.get(accountId));
	}
	
	public List<BankAccountEntity> bankAccountEntityAsList(){
		Collection<BankAccountData> values = bankAccounts.values();
		List<BankAccountEntity> result = new ArrayList<>();
		for (BankAccountData bankAccountData : values) {
			result.add(bankAccountData.getBankAccount());
		}
		return result;
	}
}
