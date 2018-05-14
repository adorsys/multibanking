package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.BankAccessData;
import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.web.account.BookingController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 08.05.18 at 15:49.
 */
public class MB_006_BookingsTest extends MB_BaseTest {
     @Test
    public void load_synched_account_retunrs_list_of_booking_per_period() {
     	BankAccessData accessData = MB_005_BankAccountTest.createAccessDataWith2BankAccounts(this);
     	BankAccessID bankAccessID = new BankAccessID(accessData.getBankAccess().getId());
     	accessData.getBankAccounts().forEach(accountData -> {
     		BankAccountID bankAccountID = new BankAccountID(accountData.getBankAccount().getId());
     		MB_005_BankAccountTest.syncBankAccount204(this, bankAccessID, bankAccountID);
     	});

        List<BankAccountData> bankAccountData = MB_005_BankAccountTest.loadAccessDataWith2BankAccounts(this).getBankAccounts();
        final CatgoryCount cc = new CatgoryCount();
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
            	ResponseEntity<BookingEntity[]> bookingResponse = this.testRestTemplate.getForEntity(uri, BookingEntity[].class);
            	BookingEntity[] bookings = bookingResponse.getBody();
            	for (int i = 0; i < bookings.length; i++) {
					BookingEntity booking = bookings[i];
					if(booking.getBookingCategory()!=null){
						cc.categoryFound = cc.categoryFound+1;
						LOGGER.info("booking with Category :" + booking.getBookingCategory().getMainCategory());
					}
				}
            });
        });
        Assert.assertTrue(cc.categoryFound>0);
    }
     
     static class CatgoryCount{
    	 int categoryFound;
     }
}
