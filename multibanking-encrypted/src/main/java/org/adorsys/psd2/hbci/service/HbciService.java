package org.adorsys.psd2.hbci.service;

import java.util.List;

import org.adorsys.psd2.common.spi.EncryptionService;
import org.adorsys.psd2.hbci.domain.EncryptedHbciLoadAccountRequest;
import org.adorsys.psd2.hbci.domain.EncryptedHbciLoadBookingsRequest;
import org.adorsys.psd2.hbci.domain.EncryptedListOfHbciBankAccounts;
import org.adorsys.psd2.hbci.domain.EncryptedListOfHbciBookings;
import org.adorsys.psd2.hbci.domain.HbciLoadAccountsRequest;
import org.adorsys.psd2.hbci.domain.HbciLoadBookingsRequest;

import domain.BankAccount;
import domain.Booking;
import hbci4java.Hbci4JavaBanking;
import hbci4java.OnlineBankingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;

@Api(value = "/v1/hbci", tags={"Access To Account HBCI"}, authorizations = @Authorization(value = "BearerToken"), description = "HBCI Frontend access to payment data")
public class HbciService {

	private OnlineBankingService onlineBankingService = new Hbci4JavaBanking();
	
	private EncryptionService encryptionService;

	public HbciService(EncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	@ApiOperation(value = "accounts", notes = "Load all bank accounts associated with given baking acess data")
	@ApiResponses(value = { @ApiResponse (code = 200, message = "Ok", response=EncryptedListOfHbciBankAccounts.class),
			@ApiResponse(code = 400, message = "Bad request", responseHeaders=@ResponseHeader(name="ERROR_KEY", description="BAD_REQUEST"))})
	public EncryptedListOfHbciBankAccounts loadBankAccounts(@ApiParam(value="The encrypted bank access object") EncryptedHbciLoadAccountRequest encryptedRequest) {
		HbciLoadAccountsRequest request = encryptionService.decrypt(encryptedRequest.getJweString(), HbciLoadAccountsRequest.class);
		List<BankAccount> bancAccountList = onlineBankingService.loadBankAccounts(request.getBankAccess(), request.getPin());

		String encryptedJwe = encryptionService.encrypt(bancAccountList, request);
		EncryptedListOfHbciBankAccounts resp = new EncryptedListOfHbciBankAccounts();
		resp.setJweString(encryptedJwe);
		return resp;
	}

	@ApiOperation(value = "bookings", notes = "Load bookings associated with given bank account")
	@ApiResponses(value = { @ApiResponse (code = 200, message = "Ok", response=EncryptedListOfHbciBookings.class),
			@ApiResponse(code = 400, message = "Bad request", responseHeaders=@ResponseHeader(name="ERROR_KEY", description="BAD_REQUEST"))})
	public EncryptedListOfHbciBookings loadPostings(@ApiParam(value="The encrypted bank access object") EncryptedHbciLoadBookingsRequest encryptedRequest) {
		HbciLoadBookingsRequest request = encryptionService.decrypt(encryptedRequest.getJweString(), HbciLoadBookingsRequest.class);
		List<Booking> bookingList = onlineBankingService.loadBookings(request.getBankAccess(), request.getBankAccount(), request.getPin());

		String encryptedJwe = encryptionService.encrypt(bookingList, request);
		EncryptedListOfHbciBookings resp = new EncryptedListOfHbciBookings();
		resp.setJweString(encryptedJwe);
		return resp;
	}
}
