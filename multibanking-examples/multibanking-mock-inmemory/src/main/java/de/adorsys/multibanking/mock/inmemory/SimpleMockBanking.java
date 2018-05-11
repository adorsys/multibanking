package de.adorsys.multibanking.mock.inmemory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.domain.BookingCategoryData;
import de.adorsys.multibanking.domain.XLSBank;
import de.adorsys.multibanking.loader.BankAccesLoader;
import de.adorsys.multibanking.loader.BankAccountLoader;
import de.adorsys.multibanking.loader.BookingLoader;
import de.adorsys.multibanking.loader.DataSheetLoader;
import de.adorsys.multibanking.loader.MockBankCatalogue;
import de.adorsys.multibanking.loader.StandingOrderLoader;
import de.adorsys.onlinebanking.mock.MockBanking;
import domain.Bank;
import domain.BankAccess;
import domain.BankAccount;
import domain.BankApiUser;
import domain.Booking;
import domain.LoadBookingsResponse;
import domain.StandingOrder;

/**
 * Mock Banking operating on the base of a json file.
 * @author fpo
 *
 */
public class SimpleMockBanking extends MockBanking {
	
	public SimpleMockBanking() {
		try {
			load(null, null, null);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public SimpleMockBanking(InputStream bookingCategoryStream, InputStream banksStream, InputStream bookingsStream) {
		try {
			load(bookingCategoryStream, banksStream, bookingsStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public List<BankAccount> loadBankAccounts(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
			String pin, boolean storePin) {
		Map<String, BankAccount> bankAccounts = data.getBankAccountMap(bankAccess.getBankLogin());
		if(bankAccounts==null) return Collections.emptyList();
		return new ArrayList<>(bankAccounts.values());
	}

	@Override
	public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
			BankAccount bankAccount, String pin) {
		String bankLogin = bankAccess.getBankLogin();
		String iban = bankAccount.getIban();
		Map<String, List<Booking>> bookingMap = data.getBookingMap(bankLogin);
		List<Booking> bookings = null;
		if(bookingMap!=null) {
			bookings = bookingMap.get(iban);
		}
		if(bookings==null)bookings=Collections.emptyList();
		
		Map<String, Map<String, StandingOrder>> standingOrderMap = data.getStandingOrderMap(bankLogin);
		List<StandingOrder> standingOrders = null;
		if(standingOrderMap!=null){
			Map<String, StandingOrder> map = standingOrderMap.get(iban);
			if(map!=null)standingOrders = new ArrayList<>(map.values());
		}
		if(standingOrders==null)standingOrders=Collections.emptyList();
		
		return LoadBookingsResponse.builder().bookings(bookings).standingOrders(standingOrders).build();

	}

	
	private BookingCategoryData bookingCategoryData;
	private List<? extends Bank> banks;
	private ObjectMapper mapper = new ObjectMapper();
	private DataMap data = new DataMap();
	private void load(InputStream bookingCategoryStream, InputStream banksStream, InputStream bookingsStream) throws IOException {
		if(bookingCategoryStream==null)
			bookingCategoryStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("booking_category.json");
		
		bookingCategoryData = mapper.readValue( bookingCategoryStream, BookingCategoryData.class );
		
		if(banksStream==null)
			banksStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("mock_bank.json");
		
		banks = mapper.readValue(banksStream, new TypeReference<List<XLSBank>>(){});

		BankAccesLoader bankAccesLoader = new BankAccesLoader(data);
		MockBankCatalogue bankCatalogue = new MockBankCatalogue();
		bankCatalogue.setBanks(banks);
		BankAccountLoader bankAccountLoader = new BankAccountLoader(data, bankCatalogue);
		BookingLoader bookingLoader = new BookingLoader(data);
		StandingOrderLoader standingOrderLoader = new StandingOrderLoader(data);
		DataSheetLoader dataSheetLoader = new DataSheetLoader(bankAccesLoader, bankAccountLoader, bookingLoader, standingOrderLoader);
		
		if(bookingsStream==null)
			bookingsStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("test_data.xls");
		
		dataSheetLoader.loadDataSheet(bookingsStream);
	}
	
}
