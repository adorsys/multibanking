package de.adorsys.multibanking.web.account;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.exception.UserNotFoundException;
import de.adorsys.multibanking.exception.domain.ErrorConstants;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BankAccountBasedController;
import de.adorsys.multibanking.web.common.BaseController;
import domain.BankApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-20 11:45
 */
@UserResource
@RestController
@RequestMapping(path = BankAccountController.BASE_PATH)
@Api(value = BankAccountController.BASE_PATH, 
	tags = "MB-005 - Bank Accounts", description="Manages access to a single bank account. Does not allow reading a single bank account data. Bank account information are contained in the user data object.")
public class BankAccountController extends BankAccountBasedController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/bankaccesses/{accessId}/accounts"; 
	private static final String SYNC_SEGMENT = "/{accountId}/sync"; 
	public static final String SYNC_PATH = BankAccountController.BASE_PATH + SYNC_SEGMENT; 

    @Autowired
    private BookingService bookingService;

    @RequestMapping(path = SYNC_SEGMENT, method = RequestMethod.PUT)
    @ApiOperation(value="Synchronize bookings", notes= "Synchronize the user booking with the remote bank account. Generally load all new booking since the last synch. Uses the booking date to splitt and store bookings into configured booking periods files e.g. yearly, quaterly."
    		+ " Will generally return a 204 for a sucessful request. Will return a 102 if another synch is in progress.")
    @ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, 
    		message = "Bank account with provided id successfuly setup for synchronization"),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_PRECONDITION_FAILED, message = UserNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = ResourceNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = SyncInProgressException.SC_PROCESSING, message = SyncInProgressException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_FORBIDDEN, message = InvalidBankAccessException.MESSAGE_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = ErrorConstants.ERR_HTTP_CODE_BAD_REQUEST_DOC, response = MultibankingError.class)})
    public HttpEntity<Void> syncBookings(
    		@ApiParam(name = "accessId", required=true,
        	value = "The identifier of the bank access container the bank account.", example="3c149076-13c4-4190-ace3-e30bf8f61526")
            @PathVariable String accessId,
    		@ApiParam(name = "accountId", required=true, 
        	value = "The identifier of the bank account in the scope of the containing bank access.", example="DE81199999993528307800")
            @PathVariable String accountId,
    		@ApiParam(name = "pin",  required=false,
	        	value = "The password of the corresponding online banking account, in case this was not saved on the server", 
	        	example="12345")
            @RequestBody(required = false) String pin) {

    	checkBankAccountExists(accessId, accountId);
        checkSynch(accessId, accountId);

        BankApi bankApi=null;
		bookingService.syncBookings(accessId, accountId, bankApi, pin);

        return new ResponseEntity<>(userDataLocationHeader(),HttpStatus.NO_CONTENT);
    }
}
