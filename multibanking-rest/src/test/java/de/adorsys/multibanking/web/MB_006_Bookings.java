package de.adorsys.multibanking.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.multibanking.domain.BankAccountData;
import de.adorsys.multibanking.domain.BookingFile;
import de.adorsys.multibanking.web.account.BookingController;
import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;

/**
 * Created by peter on 08.05.18 at 15:49.
 */
public class MB_006_Bookings extends MB_BaseTest {
//     @Test
    // TODO läuft nicht wegen https://jira.adorsys.de/browse/MUL-272

    public void test_1() {
        URI location = MB_004_BankAccess.createBankAccess(this, theBeckerTuple);
        UserDataStructure userDataStructure = MB_004_BankAccess.loadBankAccess(this, location);
        List<BankAccessID> bankAccessIDs = userDataStructure.getBankAccessIDs();
        Assert.assertEquals(1, bankAccessIDs.size());
        List<BankAccountID> bankAccountIDs = userDataStructure.getBankAccountIDs(bankAccessIDs.get(0));
        Assert.assertEquals(2, bankAccountIDs.size());
        
        bankAccountIDs.forEach(bankAccountID -> {
            LOGGER.info("found bank-AccountID:" + bankAccountID.toString());
            URI uri = MB_005_BankAccount.bankAccountPath(this, bankAccessIDs.get(0)).pathSegment(bankAccountID.getValue()).pathSegment("sync").build().toUri();
            String pin = null;
            // Hello Peter, this synch is working. Then we must expect 204. 102 only happens when we
            // Send a ynch while anotherone is still working.
            this.setNextExpectedStatusCode(204);
            LOGGER.info("PUT TO uri:" + uri);
            this.testRestTemplate.put(uri, pin);
        });
        
        
        List<BankAccountData> bankAccountData = MB_005a_BankAccount.getBankAccountData(this);
        bankAccountData.forEach(bad -> {
        	Map<String, BookingFile> bookingFiles = bad.getBookingFiles();
        	Assume.assumeNotNull(bookingFiles);
        	Assume.assumeFalse(bookingFiles.isEmpty());
        	// 
        	BankAccountID bankAccountID = new BankAccountID(bad.getBankAccount().getId());
        	
            LOGGER.info("found bank-AccountID:" + bankAccountID);
            bookingFiles.values().forEach(bookingFile -> {
            	String period = bookingFile.getPeriod();
            	URI uri = bankAccountPath(this, bankAccessIDs.get(0), bankAccountID).queryParam("period",period).build().toUri();
            	this.setNextExpectedStatusCode(200);
            	LOGGER.info("GET TO uri:" + uri);
            	ResponseEntity<String> bookings = this.testRestTemplate.getForEntity(uri, String.class);
            	LOGGER.info("bookings:" + bookings.getBody());
            });
        });
        
    }

    private static UriComponentsBuilder bankAccountPath(MB_BaseTest base, BankAccessID accessID, BankAccountID accountID) {
        String path = BookingController.BASE_PATH;
        path = path.replace("{accessId}", accessID.getValue());
        path = path.replace("{accountId}", accountID.getValue());
        return base.path(path);
    }

}
