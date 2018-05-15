package de.adorsys.multibanking.loader;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import de.adorsys.multibanking.mock.loader.BankAccesLoader;
import de.adorsys.multibanking.mock.loader.BankAccountLoader;
import de.adorsys.multibanking.mock.loader.BookingLoader;
import de.adorsys.multibanking.mock.loader.DataSheetLoader;
import de.adorsys.multibanking.mock.loader.MockBankCatalogue;
import de.adorsys.multibanking.mock.loader.StandingOrderLoader;
import de.adorsys.multibanking.test.base.BaseTest;

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
		Assert.assertTrue(data.access(bankLogin).isPresent());
		Assert.assertEquals(2, data.accessOrException(bankLogin).countAccounts());
		Assert.assertTrue(data.accessOrException(bankLogin).accountData("DE81199999993528307800").isPresent());
		Assert.assertEquals(62, data.accessOrException(bankLogin).accountDataOrException("DE81199999993528307800").bookings().size());
		Assert.assertEquals(7, data.accessOrException(bankLogin).accountDataOrException("DE12199999994076397393").bookings().size());

		Assert.assertEquals(5, data.accessOrException(bankLogin).accountDataOrException("DE81199999993528307800").standingOrders().size());
		Assert.assertEquals(1, data.accessOrException(bankLogin).accountDataOrException("DE12199999994076397393").standingOrders().size());
	}

}
