package de.adorsys.multibanking.mock.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.BankAccount;
import domain.Booking;
import domain.StandingOrder;

public class BankAccountData {

	private final BankAccount bankAccount;
	
	/* iban, Booking*/
	private final List<Booking> bookings = new ArrayList<>();
	
	/* iban, orderId, StandingOrder*/
	private final Map<String, StandingOrder> standingOrderMap = new HashMap<>();

	public BankAccountData(BankAccount bankAccount) {
		this.bankAccount = bankAccount;
	}

	public void addStandingOrders(String iban, StandingOrder standingOrder) {
		standingOrderMap.put(standingOrder.getOrderId(), standingOrder);
	}

	public void addBooking(Booking booking) {
		bookings.add(booking);
	}


	public Map<String, StandingOrder> standingOrders() {
		return standingOrderMap;
	}

	public List<Booking> bookings() {
		return bookings;
	}

	public BankAccount getBankAccount() {
		return bankAccount;
	}
}
