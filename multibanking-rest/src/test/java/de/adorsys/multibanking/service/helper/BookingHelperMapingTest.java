package de.adorsys.multibanking.service.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import de.adorsys.multibanking.domain.AccountSynchPref;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.BookingFilePeriod;
import de.adorsys.multibanking.service.old.TestUtil;
import de.adorsys.multibanking.service.testutils.RandomDate;
import de.adorsys.multibanking.utils.Ids;
import domain.BankApi;

public class BookingHelperMapingTest {
	BankAccessEntity accessEntity = TestUtil.getBankAccessEntity(Ids.uuid(), Ids.uuid(), "19999999", "0000");
	BankAccountEntity accountEntity = TestUtil.getBankAccountEntity(accessEntity, Ids.uuid());

	@Test
	public void testMap400BookingsOnWeeks() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.WEEK));
		Assert.assertTrue(mapBookings.size() <= 53);
	}

	@Test
	public void testMap400BookingsOnMonths() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.MONTH));
		Assert.assertTrue(mapBookings.size() <= 12);
	}

	@Test
	public void testMap400BookingsOnQuarters() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.QUATER));
		Assert.assertTrue(mapBookings.size() <= 4);
	}
	@Test
	public void testMap400BookingsOnSemesters() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.SEMESTER));
		Assert.assertTrue(mapBookings.size() <= 2);
	}
	@Test
	public void testMap400BookingsOnYears() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.YEAR));
		Assert.assertTrue(mapBookings.size() <= 1);
	}

	@Test
	public void testMap400BookingsOnAll() {
		Map<String, List<BookingEntity>> mapBookings = bookings(400, 2012, AccountSynchPref.instance(BookingFilePeriod.ALL));
		Assert.assertTrue(mapBookings.size() <= 1);
	}
	
	private Map<String, List<BookingEntity>> bookings(int n, int year, AccountSynchPref accountSynchPref){
		List<BookingEntity> bookings = new ArrayList<>();
		for(int i=0; i<n; i++){
			LocalDate randomDay = RandomDate.randomDay(year);
			bookings.add(TestUtil.getBookingEntity(accountEntity, BankApi.MOCK, randomDay));
		}
		Map<String, List<BookingEntity>> mapBookings = BookingHelper.mapBookings(accountEntity, accountSynchPref, bookings);
		return new TreeMap<>(mapBookings);		
	}
}
