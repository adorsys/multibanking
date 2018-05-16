package de.adorsys.multibanking.web;

import de.adorsys.multibanking.web.base.entity.BankAccessID;
import de.adorsys.multibanking.web.base.entity.BankAccountID;
import de.adorsys.multibanking.web.base.entity.UserDataStructure;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Created by peter on 08.05.18 at 15:49.
 */
public class MB_006_BookingsTest extends MB_BaseTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(MB_006_BookingsTest.class);
	public final static String BOOKING_URI = "/api/v1/bankaccesses/{accessId}/accounts/{accountId}/bookings";

	@Test
	public void load_synched_account_retunrs_list_of_booking_per_period() {
		URI location = MB_004_BankAccessTest.createBankAccess(this, theBeckerTuple);
		UserDataStructure userDataStructure = MB_005_BankAccountTest.accessDataWith2BankAccounts(this, location);
		BankAccessID firstBankAccessID = userDataStructure.getBankAccessIDs().get(0);
		for (BankAccountID bankAccountID : userDataStructure.getBankAccountIDs(firstBankAccessID)) {
			Assert.assertFalse(userDataStructure.getBookingPeriods(firstBankAccessID, bankAccountID).isPresent());
		}

		for (BankAccountID bankAccountID : userDataStructure.getBankAccountIDs(firstBankAccessID)) {
			MB_005_BankAccountTest.syncBankAccount204(this, firstBankAccessID, bankAccountID);
		}

		userDataStructure = MB_005_BankAccountTest.accessDataWith2BankAccounts(this, location);
		for (BankAccountID bankAccountID : userDataStructure.getBankAccountIDs(firstBankAccessID)) {
			Optional<List<String>> bookingPeriods = userDataStructure.getBookingPeriods(firstBankAccessID, bankAccountID);
			Assert.assertTrue(bookingPeriods.isPresent());
			for (String period : bookingPeriods.get()) {
				LOGGER.debug("period is " + period);
				URI uri = path(BOOKING_URI).queryParam("period", period).build(firstBankAccessID.getValue(), bankAccountID.getValue());
				this.setNextExpectedStatusCode(200);
				LOGGER.debug("GET TO uri:" + uri);
				ResponseEntity<String> bookings = this.testRestTemplate.getForEntity(uri, String.class);
				LOGGER.debug("bookings:" + bookings.getBody());
			}
		}
	}

}
