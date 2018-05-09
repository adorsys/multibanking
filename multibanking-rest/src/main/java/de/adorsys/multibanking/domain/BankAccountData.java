package de.adorsys.multibanking.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import domain.BankAccount.SyncStatus;
import lombok.Data;

/**
 * Holds data associated with a bank account.
 * 
 * @author fpo
 *
 */
@Data
public class BankAccountData {
	
	private BankAccountEntity bankAccount;
	
	private Map<String, BookingFile> bookingFiles = new HashMap<>();

	private AccountSynchPref accountSynchPref;
	
	private Map<String, StandingOrderEntity> standingOrders;
	
    private List<ContractEntity> contracts = new ArrayList<>();

    private AccountAnalyticsEntity analytic;
    
	public void update(Collection<BookingFile> newEntries) {
		bookingFiles.putAll(bookingPeriodsMap(newEntries));
	}
	
	private static Map<String, BookingFile> bookingPeriodsMap(Collection<BookingFile> bookingFileExts){
		return bookingFileExts.stream().collect(Collectors.toMap(BookingFile::getPeriod, Function.identity()));
	}
    
	public void updateSyncStatus(SyncStatus syncStatus) {
		LocalDateTime now = LocalDateTime.now();
		bankAccount.setSyncStatus(syncStatus);
		if(SyncStatus.READY==syncStatus){
			bankAccount.setLastSync(now);
		}
	}
}
