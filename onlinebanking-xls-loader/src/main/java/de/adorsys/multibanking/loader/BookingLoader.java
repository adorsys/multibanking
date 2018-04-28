package de.adorsys.multibanking.loader;

import static de.adorsys.multibanking.utils.CellUtils.bigDecimalCell;
import static de.adorsys.multibanking.utils.CellUtils.booleanCell;
import static de.adorsys.multibanking.utils.CellUtils.localDate;
import static de.adorsys.multibanking.utils.CellUtils.stringCell;
import static de.adorsys.multibanking.utils.CellUtils.stringFromNumCell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;

import de.adorsys.multibanking.domain.BookingCategoryData;
import de.adorsys.multibanking.service.XLSBookingService;
import domain.BankAccount;
import domain.BankApi;
import domain.Booking;
import domain.BookingCategory;

public class BookingLoader {
	private XLSBookingService bookingService;

	public BookingLoader(XLSBookingService bookingService) {
		this.bookingService = bookingService;
	}


	public void update(Row row) {
		String bankLogin=stringCell(row, 0, false);		
		String iban = stringCell(row, 1, false);;

		Booking booking = new Booking();
		booking.setBankApi(BankApi.MOCK);
		booking.setExternalId(UUID.randomUUID().toString());
		booking.setAmount(bigDecimalCell(row, 2, false));
		booking.setBalance(bigDecimalCell(row, 3, true));
		booking.setOrigValue(bigDecimalCell(row, 4, true));
		booking.setChargeValue(bigDecimalCell(row, 5, true));
		booking.setBookingDate(localDate(row, 6, false));
		booking.setValutaDate(localDate(row, 7, false));
		booking.setTransactionCode(stringFromNumCell(row, 8, true));
		booking.setReversal(booleanCell(row, 9, true));
		booking.setStandingOrder(booleanCell(row, 10, true));
		booking.setAdditional(stringCell(row, 11, true));
		booking.setUsage(stringCell(row, 12, false));
		booking.setText(stringCell(row, 13, true));
		booking.setSepa(booleanCell(row, 14, true));
		booking.setMandateReference(stringCell(row, 15, true));
		booking.setInstRef(stringCell(row, 16, true));
		booking.setCreditorId(stringCell(row, 17, true));
		booking.setCustomerRef(stringCell(row, 18, true));

		BankAccount bankAccount = new BankAccount();
		bankAccount.setOwner(stringCell(row, 19, true));
		bankAccount.setName(bankAccount.getOwner());
		bankAccount.setIban(stringCell(row, 20, false));
		bankAccount.setBic(stringCell(row, 21, true));
		bankAccount.setBankName(stringCell(row, 22, true));
		booking.setOtherAccount(bankAccount);
		bookingService.addBooking(bankLogin, iban, booking);
	}

	public static void setContract(List<Booking> bookings, BookingCategoryData contracts){
		if(contracts==null) return ;
		List<BookingCategory> incomeCategory = contracts.getInsuranceCategory();
		int i = 0;
		for (BookingCategory it : incomeCategory) {
			double amount = new Random().nextDouble()*100;
			for (int j = 0; j < 4; j++) {
				if(i>=bookings.size()) break ;
				bookings.get(i).setBookingDate(LocalDate.now().minusMonths(j));
				bookings.get(i).setValutaDate(LocalDate.now().minusMonths(j));
				bookings.get(i).setBookingCategory(it);
				bookings.get(i).setAmount(BigDecimal.valueOf(amount));
				bookings.get(i).setUsage(it.getSpecification());
				i++;
			}
		}
	}
}
