package de.adorsys.multibanking.mock.service;

import domain.BankAccess;

public interface XLSBankAccessService {

	boolean hasBankAccessForBankCode(String bankLogin, String bankCode);

	void addBankAccess(String bankLogin, String pin, BankAccess bankAccess);

}
