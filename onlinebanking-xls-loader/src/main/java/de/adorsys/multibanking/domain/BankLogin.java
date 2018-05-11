package de.adorsys.multibanking.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.BankAccess;
import domain.BankAccount;
import domain.Booking;
import domain.StandingOrder;

public class BankLogin {

	/* iban, BankAccess*/
	private final Map<String, BankAccess> bankAccessMapByBankLogin = new HashMap<>();
	private final Map<String, BankAccess> bankAccessMapByBankCode = new HashMap<>();
	
	/* iban, BankAccount*/
	private final Map<String, BankAccount> bankAccountMap = new HashMap<>();

	/* iban, Booking*/
	private final Map<String, List<Booking>> bookingMap = new HashMap<>();
	
	/* iban, orderId, StandingOrder*/
	private final Map<String, Map<String, StandingOrder>> standingOrderMap = new HashMap<>();

	public void addStandingOrders(String iban, StandingOrder standingOrder) {
		if(!standingOrderMap.containsKey(iban))standingOrderMap.put(iban, new HashMap<>());
		standingOrderMap.get(iban).put(standingOrder.getOrderId(), standingOrder);
	}

	public void addBooking(String iban, Booking booking) {
		if(!bookingMap.containsKey(iban))bookingMap.put(iban, new ArrayList<>());
		bookingMap.get(iban).add(booking);
	}

	public void addBankAccount(BankAccount bankAccount) {
		bankAccountMap.put(bankAccount.getIban(), bankAccount);
	}

	public boolean hasBankAccessForBankCode(String bankCode) {
		return bankAccessMapByBankCode.containsKey(bankCode);
	}

	public void addBankAccess(String pin, BankAccess bankAccess) {
		bankAccessMapByBankLogin.put(bankAccess.getBankLogin(), bankAccess);
		bankAccessMapByBankCode.put(bankAccess.getBankCode(), bankAccess);
	}

	public Map<String, Map<String, StandingOrder>> getStandingOrderMap() {
		return standingOrderMap;
	}

	public Map<String, List<Booking>> getBookingMap() {
		return bookingMap;
	}

	public Map<String, BankAccount> getBankAccountMap() {
		return bankAccountMap;
	}

	public Map<String, BankAccess> getBankAccessMapByBankLogin() {
		return bankAccessMapByBankLogin;
	}

	public Map<String, BankAccess> getBankAccessMapByBankCode() {
		return bankAccessMapByBankCode;
	}
}
