package de.adorsys.multibanking.service;

import domain.Booking;

public interface XLSBookingService {

	void addBooking(String bankLogin, String iban, Booking extBooking);

}
