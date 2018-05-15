package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.adorsys.multibanking.domain.BankAccountData;
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

	private List<BankAccountData> bankAccounts = new ArrayList<>();

	private AccountSynchPref accountSynchPref;

	public Optional<BankAccountData> getBankAccount(String accountId) {
        for (BankAccountData bankAccountData : bankAccounts) {
            if (bankAccountData.getBankAccount().getId().equals(accountId)) {
                return Optional.of(bankAccountData);
            }
        }
		return Optional.empty();
	}

	public List<BankAccountEntity> bankAccountEntityAsList(){
		List<BankAccountEntity> result = new ArrayList<>();
		for (BankAccountData bankAccountData : bankAccounts) {
			result.add(bankAccountData.getBankAccount());
		}
		return result;
	}

    public boolean containsKey(String accountId) {
        for (BankAccountData bankAccountData : bankAccounts) {
            if (bankAccountData.getBankAccount().getId().equals(accountId)) {
                return true;
            }
        }
        return false;
    }
}
