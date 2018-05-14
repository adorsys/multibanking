package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.web.account.BookingController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author fpo
 */
public class MB_007_AnalyticsTest extends MB_BaseTest {
     @Test
    public void load_synched_account_retunrs_list_of_booking_with_analytics() {
     	BankAccessData accessData = MB_005_BankAccountTest.createAccessDataWith2BankAccounts(this);
     	BankAccessID bankAccessID = new BankAccessID(accessData.getBankAccess().getId());
     	accessData.getBankAccounts().forEach(accountData -> {
     		BankAccountID bankAccountID = new BankAccountID(accountData.getBankAccount().getId());
     		MB_005_BankAccountTest.syncBankAccount204(this, bankAccessID, bankAccountID);
     	});

        List<BankAccountData> bankAccountData = MB_005_BankAccountTest.loadAccessDataWith2BankAccounts(this).getBankAccounts();
        bankAccountData.forEach(bad -> {
        	BankAccountID bankAccountID = new BankAccountID(bad.getBankAccount().getId());
        	LOGGER.info("found bank-AccountID:" + bankAccountID);
        	Map<String, BookingFile> bookingFiles = bad.getBookingFiles();
        	Assume.assumeNotNull(bookingFiles);
        	Assume.assumeFalse(bookingFiles.isEmpty());
        	
            bookingFiles.values().forEach(bookingFile -> {
            	String period = bookingFile.getPeriod();
            	URI uri = path(BookingController.BASE_PATH).queryParam("period",period).build(bankAccessID.getValue(), bankAccountID.getValue());
            	this.setNextExpectedStatusCode(200);
            	LOGGER.info("GET TO uri:" + uri);
            	ResponseEntity<String> bookings = this.testRestTemplate.getForEntity(uri, String.class);
            	LOGGER.info("bookings:" + bookings.getBody());
            });
        });
    }
}
