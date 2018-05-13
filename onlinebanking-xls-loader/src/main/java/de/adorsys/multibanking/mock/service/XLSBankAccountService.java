package de.adorsys.multibanking.mock.service;

import domain.BankAccount;

public interface XLSBankAccountService {

	void addBankAccount(String bankLogin, BankAccount bankAccount);

}
