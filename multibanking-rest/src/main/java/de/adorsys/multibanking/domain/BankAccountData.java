package de.adorsys.multibanking.domain;

import java.util.Map;

import lombok.Data;

/**
 * Holds data associated with a bank account.
 * 
 * @author fpo
 *
 */
@Data
public class BankAccountData {
	
	private BankAccountEntity bankAccount;
	
	private AccountSynchResult synchResult = new AccountSynchResult();

	private AccountSynchPref accountSynchPref;
	
	private Map<String, StandingOrderEntity> standingOrders;
}
