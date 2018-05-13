package de.adorsys.multibanking.mock.inmemory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.mock.domain.BankAccessData;
import de.adorsys.multibanking.mock.domain.BankAccountData;
import de.adorsys.multibanking.mock.domain.BookingCategoryData;
import de.adorsys.multibanking.mock.domain.MockAccount;
import de.adorsys.multibanking.mock.domain.XLSBank;
import de.adorsys.multibanking.mock.loader.BankAccesLoader;
import de.adorsys.multibanking.mock.loader.BankAccountLoader;
import de.adorsys.multibanking.mock.loader.BookingLoader;
import de.adorsys.multibanking.mock.loader.DataSheetLoader;
import de.adorsys.multibanking.mock.loader.MockBankCatalogue;
import de.adorsys.multibanking.mock.loader.StandingOrderLoader;
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
	private BookingCategoryData bookingCategoryData;
	private List<? extends Bank> banks;
	private ObjectMapper mapper = new ObjectMapper();
	private MockAccount data = new MockAccount();

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
		return data.loadBankAccounts(bankAccess, bankCode, pin);
	}

	@Override
	public LoadBookingsResponse loadBookings(BankApiUser bankApiUser, BankAccess bankAccess, String bankCode,
			BankAccount bankAccount, String pin) {
		String bankLogin = bankAccess.getBankLogin();
		String iban = bankAccount.getIban();
		BankAccessData bankAccessData = data.accessOrException(bankLogin);
		bankAccessData.checkPin(pin);
		BankAccountData accountData = data.accessOrException(bankLogin).accountDataOrException(iban);
		List<Booking> bookings =  accountData.bookings();
		List<StandingOrder> standingOrders = new ArrayList<>(accountData.standingOrders().values());
		return LoadBookingsResponse.builder().bookings(bookings).standingOrders(standingOrders).build();

	}
	
	private void load(InputStream bookingCategoryStream, InputStream banksStream, InputStream bookingsStream) throws IOException {
		if(bookingCategoryStream==null)
			bookingCategoryStream = SimpleMockBanking.class.getResourceAsStream("/booking_category.json");
		
		bookingCategoryData = mapper.readValue( bookingCategoryStream, BookingCategoryData.class );
		
		if(banksStream==null)
			banksStream = SimpleMockBanking.class.getResourceAsStream("/mock_bank.json");
		
		banks = mapper.readValue(banksStream, new TypeReference<List<XLSBank>>(){});

		BankAccesLoader bankAccesLoader = new BankAccesLoader(data);
		MockBankCatalogue bankCatalogue = new MockBankCatalogue();
		bankCatalogue.setBanks(banks);
		BankAccountLoader bankAccountLoader = new BankAccountLoader(data, bankCatalogue);
		BookingLoader bookingLoader = new BookingLoader(data);
		StandingOrderLoader standingOrderLoader = new StandingOrderLoader(data);
		DataSheetLoader dataSheetLoader = new DataSheetLoader(bankAccesLoader, bankAccountLoader, bookingLoader, standingOrderLoader);
		
		if(bookingsStream==null)
			bookingsStream = SimpleMockBanking.class.getResourceAsStream("/mock_bank.xls");
		
		dataSheetLoader.loadDataSheet(bookingsStream);
	}
	
}
