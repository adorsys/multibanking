package de.adorsys.multibanking.loader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.adorsys.multibanking.test.base.BaseTest;
import domain.Booking;
import domain.StandingOrder;

public class DataSheetLoaderTest extends BaseTest {

	@Test
	public void testLoadDataSheet() {
		
		BankAccesLoader bankAccesLoader = new BankAccesLoader(data);
		MockBankCatalogue bankCatalogue = new MockBankCatalogue();
		bankCatalogue.setBanks(banks);
		BankAccountLoader bankAccountLoader = new BankAccountLoader(data, bankCatalogue);
		BookingLoader bookingLoader = new BookingLoader(data);
		StandingOrderLoader standingOrderLoader = new StandingOrderLoader(data);
		DataSheetLoader dataSheetLoader = new DataSheetLoader(bankAccesLoader, bankAccountLoader, bookingLoader, standingOrderLoader);
		InputStream dataStream = DataSheetLoaderTest.class.getClassLoader().getResourceAsStream("test_data.xls");
		dataSheetLoader.loadDataSheet(dataStream);
		
		String bankLogin = "m.becker";
		Assert.assertEquals(1, data.getBankAccessMapByBankCode(bankLogin).size());
		Assert.assertEquals(2, data.getBankAccountMap(bankLogin).size());
		
		Assert.assertEquals(2, data.getBookingMap(bankLogin).size());

		List<Booking> bookings1 = data.getBookingMap(bankLogin).get("DE81199999993528307800");
		Assert.assertNotNull(bookings1);
		Assert.assertEquals(62, bookings1.size());
		List<Booking> bookings2 = data.getBookingMap(bankLogin).get("DE12199999994076397393");
		Assert.assertNotNull(bookings2);
		Assert.assertEquals(7, bookings2.size());

		Assert.assertEquals(2, data.getStandingOrderMap(bankLogin).size());
		
		Map<String, StandingOrder> standingOrder1 = data.getStandingOrderMap(bankLogin).get("DE81199999993528307800");
		Assert.assertNotNull(standingOrder1);
		Assert.assertEquals(5, standingOrder1.size());
		Map<String, StandingOrder> standingOrder2 = data.getStandingOrderMap(bankLogin).get("DE12199999994076397393");
		Assert.assertNotNull(standingOrder2);
		Assert.assertEquals(1, standingOrder2.size());
	}

}
