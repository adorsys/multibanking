package de.adorsys.multibanking.domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.adorsys.multibanking.domain.common.AbstractId;
import de.adorsys.multibanking.utils.DateConstants;
import domain.BankAccount.SyncStatus;
import lombok.Data;

/**
 * Stores the summary of an account synch. File resides in the directory of the containing account.
 * 
 * @author fpo 2018-03-20 04:13
 *
 */
@Data
public class AccountSynchResult extends AbstractId {

	private Map<String, BookingFile> bookingFiles = new HashMap<>();
	
	private SyncStatus syncStatus;

	@JsonFormat(pattern = DateConstants.DATE_TIME_FORMAT_ISO8601_ZONELESS)
	private LocalDateTime statusTime;
	
	@JsonFormat(pattern = DateConstants.DATE_TIME_FORMAT_ISO8601_ZONELESS)
	private LocalDateTime lastSynch;
	
	public AccountSynchResult update(Collection<BookingFile> newEntries) {
		bookingFiles.putAll(bookingPeriodsMap(newEntries));
		return this;
	}
	
	private static Map<String, BookingFile> bookingPeriodsMap(Collection<BookingFile> bookingFileExts){
		return bookingFileExts.stream().collect(Collectors.toMap(BookingFile::getPeriod, Function.identity()));
	}
}
