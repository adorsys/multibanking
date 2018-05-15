package de.adorsys.multibanking.mock.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import domain.BankAccess;
import domain.BankAccount;
import exception.InvalidPinException;
import exception.ResourceNotFoundException;

public class BankAccessData {
	
	private final BankAccess bankAccess;
	private final String pin;
	
	/* iban, BankAccount*/
	private final Map<String, BankAccountData> bankAccountMap = new HashMap<>();

	public BankAccessData(BankAccess bankAccess, String pin) {
		super();
		this.bankAccess = bankAccess;
		this.pin = pin;
	}
	public BankAccess getBankAccess() {
		return bankAccess;
	}
	public String getPin() {
		return pin;
	}
	
	public void checkPin(String pin){
		if(!StringUtils.equalsIgnoreCase(this.pin, pin)) throw new InvalidPinException();
	}

	public void addBankAccount(BankAccount bankAccount) {
		bankAccountMap.put(bankAccount.getIban(), new BankAccountData(bankAccount));
	}

	public Optional<BankAccountData> accountData(String iban) {
		return Optional.ofNullable(bankAccountMap.get(iban));
	}
	public BankAccountData accountDataOrException(String iban) {
		return accountData(iban).orElseThrow(() -> notFound(iban));
	}
	
	private ResourceNotFoundException notFound(String iban){
		return new ResourceNotFoundException(String.format("BankAccount with iban %s for bank login %s not found", iban, bankAccess.getBankLogin()));		
	}
	
	public Optional<BankAccountData> bankCode(String bankCode){
		return bankAccountMap.values().stream().filter(b -> StringUtils.equalsIgnoreCase(bankCode, b.getBankAccount().getBic())).findFirst();
	}
	public int countAccounts() {
		return bankAccountMap.size();
	}
	public List<BankAccount> loadBankAccounts(String bankCode) {
		return bankAccountMap.values().stream().map(BankAccountData::getBankAccount).collect(Collectors.<BankAccount> toList());
	}
}
