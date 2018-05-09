package de.adorsys.multibanking.domain;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.datetime.standard.DateTimeFormatterFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum BookingFilePeriod {
	
	ALL(){
		@Override
		public String marker(LocalDate date) {
			return "all";
		}},
	YEAR(){
		@Override
		public String marker(LocalDate date) {
			return yearMarker(date);
		}},
	SEMESTER() {
		@Override
		public String marker(LocalDate date) {
			return semesterMarker(date);
		}
	},
	QUATER() {
		@Override
		public String marker(LocalDate date) {
			return quarterMaker(date);
		}
	},
	MONTH() {
		@Override
		public String marker(LocalDate date) {
			return monthMarker(date);
		}
	},
	WEEK() {
		@Override
		public String marker(LocalDate date) {
			return weekMaker(date);
		}
	},
	DAY() {
		@Override
		public String marker(LocalDate date) {
			return dayMarker(date);
		}
	};

	public abstract String marker(LocalDate date);
	
	private static final String YEAR_FORMAT = "yyyy";
	private static final DateTimeFormatter yearFormater = new DateTimeFormatterFactory(YEAR_FORMAT).createDateTimeFormatter();
	public static final String yearMarker(LocalDate date){
		return date.format(yearFormater);
	}
	
	private static final String semesterMarker(LocalDate date){
		String year = date.format(yearFormater);
		if(date.getMonthValue()<7){
			return year + "S1";
		} else {
			return year + "S2";
		}
	}
	
	private static final String quarterMaker(LocalDate date){
		String year = date.format(yearFormater);
		switch (date.getMonthValue()) {
		case 1:
		case 2:
		case 3:
			return year + "Q1";
		case 4:
		case 5:
		case 6:
			return year + "Q2";
		case 7:
		case 8:
		case 9:
			return year + "Q3";
		case 10:
		case 11:
		case 12:
			return year + "Q4";
		default:
			throw new BaseException("Illegal month digit: > 12 or < 1");
		}
	}
	
	private static final String MONTH_FORMAT = "yyyy'M'MM";
	private static final DateTimeFormatter monthFormater = new DateTimeFormatterFactory(MONTH_FORMAT).createDateTimeFormatter();
	private static final String monthMarker(LocalDate date){
		return date.format(monthFormater);
	}
	
	private static final String WEEK_FORMAT = "YYYY'W'ww";// Intentionally used year and not week year. 
	private static final DateTimeFormatter weekFormater = new DateTimeFormatterFactory(WEEK_FORMAT).createDateTimeFormatter();
	private static final String weekMaker(LocalDate date){
		int dayOfYear = date.getDayOfYear();
		int week = dayOfYear/7;
		int cw = 1 + date.getDayOfYear()/7;
		
//		String week = date.format(weekFormater);
		// Sometime week will be back to 01 if day of year if after cw52. Then we will have
		// To manually use the cw53  of the year.
		String year = date.format(yearFormater);
//		if(!StringUtils.startsWith(week, year)){// In case week year and year are different
//			if(StringUtils.endsWith(week, "01")){
//				week = year+"W53";// We consider this the 53d cw of the year.
//			}
//		}
		return year+"W"+StringUtils.leftPad(cw+"", 2, '0');
	}

	private static final String DAY_FORMAT = "yyyyMMdd";
	private static final DateTimeFormatter dayFormater = new DateTimeFormatterFactory(DAY_FORMAT).createDateTimeFormatter();
	private static final String dayMarker(LocalDate date){
		return date.format(dayFormater);
	}
}
