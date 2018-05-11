package de.adorsys.multibanking.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DayFormat {
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
	
	public static String printDay(Date date){
		return simpleDateFormat.format(date);
	}
}
