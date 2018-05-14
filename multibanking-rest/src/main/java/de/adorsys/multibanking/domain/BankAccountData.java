package de.adorsys.multibanking.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import domain.BankAccount.SyncStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds data associated with a bank account.
 * 
 * @author fpo
 *
 */
@Data
public class BankAccountData {
	
	private BankAccountEntity bankAccount;
	
	private List<BookingFile> bookingFiles = new ArrayList<>();

	private AccountSynchPref accountSynchPref;
	
	private Map<String, StandingOrderEntity> standingOrders;
	
    private List<ContractEntity> contracts = new ArrayList<>();

    private AccountAnalyticsEntity analytic;
    
    @ApiModelProperty(value = "Time of last Synchronisation status", example="2017-12-01T12:25:44")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime syncStatusTime;
    
	public void update(Collection<BookingFile> newEntries) {
		bookingFiles.addAll(newEntries);
	}

	public void updateSyncStatus(SyncStatus syncStatus) {
		LocalDateTime now = LocalDateTime.now();
		bankAccount.setSyncStatus(syncStatus);
		syncStatusTime = now;
		// Set status if status is being set to ready.
		if(SyncStatus.READY==syncStatus)bankAccount.setLastSync(now);
	}

	public Boolean containsBookingFileOfPeriod(String period) {
		return bookingFiles.stream().filter(bookingFile -> period.equals(bookingFile.getPeriod())).count() > 0;
	}

	public Optional<BookingFile> findBookingFileOfPeriod(String period) {
		return bookingFiles.stream().filter(bookingFile -> period.equals(bookingFile.getPeriod())).findFirst();
	}

}
