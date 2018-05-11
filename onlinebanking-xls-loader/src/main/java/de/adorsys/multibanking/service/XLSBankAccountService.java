package de.adorsys.multibanking.service;

import domain.BankAccount;

public interface XLSBankAccountService {

	void addBankAccount(String bankLogin, BankAccount bankAccount);

}
