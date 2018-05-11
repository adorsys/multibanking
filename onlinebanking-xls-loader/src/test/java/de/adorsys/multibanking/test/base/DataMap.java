package de.adorsys.multibanking.test.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.adorsys.multibanking.domain.BankLogin;
import de.adorsys.multibanking.service.XLSBankAccessService;
import de.adorsys.multibanking.service.XLSBankAccountService;
import de.adorsys.multibanking.service.XLSBookingService;
import de.adorsys.multibanking.service.XLSStandingOrderService;
import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;
import domain.StandingOrder;
import exception.ResourceNotFoundException;

public class DataMap implements XLSBankAccessService, XLSBankAccountService, XLSBookingService, XLSStandingOrderService {
	
	private Map<String, BankLogin> bankLogins = new HashMap<>();

	@Override
	public void addStandingOrders(String bankLogin, String iban, StandingOrder standingOrder) {
		bankLogin(bankLogin).addStandingOrders(iban, standingOrder);
	}

	@Override
	public void addBooking(String bankLogin, String iban, Booking booking) {
		bankLogin(bankLogin).addBooking(iban, booking);
	}

	@Override
	public void addBankAccount(String bankLogin, BankAccount bankAccount) {
		bankLogin(bankLogin).addBankAccount(bankAccount);
	}

	@Override
	public boolean hasBankAccessForBankCode(String bankLogin, String bankCode) {
		try {
			return bankLogin(bankLogin).hasBankAccessForBankCode(bankCode);
		} catch(ResourceNotFoundException r){
			return false;
		}
	}

	@Override
	public void addBankAccess(String bankLogin, String pin, BankAccess bankAccess) {
		if(!hasBankLogin(bankLogin))newBankLogin(bankLogin);
		bankLogin(bankLogin).addBankAccess(pin, bankAccess);
	}

	public Map<String, Map<String, StandingOrder>> getStandingOrderMap(String bankLogin) {
		return bankLogin(bankLogin).getStandingOrderMap();
	}

	public Map<String, List<Booking>> getBookingMap(String bankLogin) {
		return bankLogin(bankLogin).getBookingMap();
	}

	public Map<String, BankAccount> getBankAccountMap(String bankLogin) {
		return bankLogin(bankLogin).getBankAccountMap();
	}

	public Map<String, BankAccess> getBankAccessMapByBankLogin(String bankLogin) {
		return bankLogin(bankLogin).getBankAccessMapByBankLogin();
	}

	public Map<String, BankAccess> getBankAccessMapByBankCode(String bankLogin) {
		return bankLogin(bankLogin).getBankAccessMapByBankCode();
	}
	
	private BankLogin bankLogin(String bankLogin){
		BankLogin bl = bankLogins.get(bankLogin);
		if(bl==null) throw new ResourceNotFoundException(String.format("BankLogin with id %s not found", bankLogin));	
		return bl;
	}

	private boolean hasBankLogin(String bankLogin){
		return bankLogins.containsKey(bankLogin);
	}
	private void newBankLogin(String bankLogin){
		bankLogins.put(bankLogin, new BankLogin());
	}
}
