package de.adorsys.multibanking.service.helper;

import java.time.LocalDate;
import java.time.Month;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.junit.Assert;
import org.junit.Test;

import de.adorsys.multibanking.domain.AccountSynchPref;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.BookingPeriod;

public class BookingHelperDateFormatTest {

	@Test(expected=BaseException.class)
	public void testPeriodNull() {
		AccountSynchPref pref = new AccountSynchPref();
		BookingEntity booking = new BookingEntity();
		BookingHelper.period(booking, pref);
	}

	@Test
	public void testPeriodall_20120212() {
		tstBooking(BookingPeriod.ALL, 2012, Month.FEBRUARY, 12, "all");
	}

	@Test
	public void testPeriod2012_20120212() {
		tstBooking(BookingPeriod.YEAR, 2012, Month.FEBRUARY, 12, "2012");
	}

	@Test
	public void testPeriod2012M02_20120212() {
		tstBooking(BookingPeriod.MONTH, 2012, Month.FEBRUARY, 12, "2012M02");
	}

	@Test
	public void testPeriod2012M02_20120229() {
		tstBooking(BookingPeriod.MONTH, 2012, Month.FEBRUARY, 29, "2012M02");
	}

	@Test
	public void testPeriod20120212_20120212() {
		tstBooking(BookingPeriod.DAY, 2012, Month.FEBRUARY, 12, "20120212");
	}

	@Test
	public void testPeriod20120229_20120229() {
		tstBooking(BookingPeriod.DAY, 2012, Month.FEBRUARY, 29, "20120229");
	}

	@Test
	public void testPeriod2012Q1_20120212() {
		tstBooking(BookingPeriod.QUATER, 2012, Month.FEBRUARY, 12, "2012Q1");
	}

	@Test
	public void testPeriod2012Q2_20120429() {
		tstBooking(BookingPeriod.QUATER, 2012, Month.APRIL, 29, "2012Q2");
	}

	@Test
	public void testPeriod2012Q3_20120729() {
		tstBooking(BookingPeriod.QUATER, 2012, Month.JULY, 29, "2012Q3");
	}

	@Test
	public void testPeriod2012Q4_20120329() {
		tstBooking(BookingPeriod.QUATER, 2012, Month.DECEMBER, 29, "2012Q4");
	}

	@Test
	public void testPeriod2012S1_20120329() {
		tstBooking(BookingPeriod.SEMESTER, 2012, Month.MARCH, 29, "2012S1");
	}

	@Test
	public void testPeriod2012S2_20121229() {
		tstBooking(BookingPeriod.SEMESTER, 2012, Month.DECEMBER, 29, "2012S2");
	}


	@Test
	public void testPeriod2012W01_20120101() {
		tstBooking(BookingPeriod.WEEK, 2012, Month.JANUARY, 1, "2012W01");
	}

	@Test
	public void testPeriod2011W01_20110104() {
		tstBooking(BookingPeriod.WEEK, 2011, Month.JANUARY, 4, "2011W01");
	}

	@Test
	public void testPeriod2010W52_20101228() {
		tstBooking(BookingPeriod.WEEK, 2010, Month.DECEMBER, 28, "2010W52");
	}
	
	@Test
	public void testPeriod2012W53_20121229() {
		tstBooking(BookingPeriod.WEEK, 2012, Month.DECEMBER, 29, "2012W53");
	}

	@Test
	public void testPeriod2012W16_20120418() {
		tstBooking(BookingPeriod.WEEK, 2012, Month.APRIL, 18, "2012W16");
	}

	@Test
	public void testPeriod2012W43_20121025() {
		tstBooking(BookingPeriod.WEEK, 2012, Month.OCTOBER, 25, "2012W43");
	}

	@Test
	public void testPeriod19XXW01() {
		int baseYear = 1970;
		for(int i=0; i<100; i++){
			int year = baseYear+i;
			tstBooking(BookingPeriod.WEEK, year, Month.JANUARY, 1, year +"W01");
		}
	}
	
	private void tstBooking(BookingPeriod period, int year, Month month, int day, String expected) {
		String periodStr = booking(period, year, month, day, expected);
		Assert.assertEquals(expected, periodStr);
	}

	private String booking(BookingPeriod period, int year, Month month, int day, String expected) {
		AccountSynchPref pref = new AccountSynchPref();
		pref.setBookingPeriod(period);
		BookingEntity booking = new BookingEntity();
		booking.setBookingDate(LocalDate.of(year, month, day));
		return BookingHelper.period(booking, pref);
	}
}
