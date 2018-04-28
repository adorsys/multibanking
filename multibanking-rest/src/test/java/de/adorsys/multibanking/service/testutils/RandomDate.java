package de.adorsys.multibanking.service.testutils;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Random;

public class RandomDate {
	static Random random = new Random();
	public static LocalDate randomDay(int year) {
		LocalDate day = LocalDate.of(year, Month.DECEMBER, 31);
		return day.minus(Period.ofDays((random.nextInt(365))));
	}
}
