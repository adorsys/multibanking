package de.adorsys.multibanking.test.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.domain.BookingCategoryData;
import de.adorsys.multibanking.domain.XLSBank;
import domain.Bank;

public class BaseTest {
	
	protected static BookingCategoryData bookingCategoryData;
	protected static List<? extends Bank> banks;
	protected static ObjectMapper mapper = new ObjectMapper();
	protected static DataMap data = new DataMap();
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		InputStream stream = BaseTest.class.getClassLoader().getResourceAsStream("booking_category.json");
		bookingCategoryData = mapper.readValue( stream, BookingCategoryData.class );

		InputStream inputStream = BaseTest.class.getClassLoader().getResourceAsStream("mock_bank.json");
		banks = mapper.readValue(inputStream, new TypeReference<List<XLSBank>>(){});
	}
	
}
