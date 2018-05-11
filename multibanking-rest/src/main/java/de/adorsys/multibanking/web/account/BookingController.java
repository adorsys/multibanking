package de.adorsys.multibanking.web.account;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.UnexistentBookingFileException;
import de.adorsys.multibanking.exception.UserNotFoundException;
import de.adorsys.multibanking.exception.domain.ErrorConstants;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BankAccountBasedController;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-20 11:46
 */
@UserResource
@RestController
@RequestMapping(path = BookingController.BASE_PATH)
@Api(value = BookingController.BASE_PATH, 
tags = "MB-006 - Bookings", description="Enable the laoding of a booking files given the booking period. The list of available booking periods is contained in the user data object.")
public class BookingController extends BankAccountBasedController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/bankaccesses/{accessId}/accounts/{accountId}/bookings"; 

    @Autowired
    private BookingService bookingService;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE }, params={"period"})
    @ApiOperation(value="Load bookings", notes = "Returns the booking file of the given booking period. The list of available booking periods is contained in the user data object.")
    @ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_OK, 
    		message = "Ok", response=BookingEntity[].class),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_PRECONDITION_FAILED, message = UserNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = ResourceNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = UnexistentBookingFileException.MESSAGE_DOC, response = MultibankingError.class)})
    public @ResponseBody ResponseEntity<ByteArrayResource> getBookings(
    		@ApiParam(name = "accessId", required=true,
    			value = "The identifier of the bank access container the bank account.", 
    			example="3c149076-13c4-4190-ace3-e30bf8f61526")
            @PathVariable String accessId,
    		@ApiParam(name = "accountId", required=true, 
        		value = "The identifier of the bank account in the scope of the containing bank access.", 
        		example="DE81199999993528307800")
            @PathVariable String accountId,
    		@ApiParam(name = "period",  required=true,
        		value = "The period file to be retured. The list of available booking periods is contained in the user data object.", 
        		example="2018")
            @RequestParam(required = true, name="period") String period
    ) {
    	checkBankAccountExists(accessId, accountId);
    	checkSynch(accessId, accountId);
    	return loadBytesForWeb(bookingService.getBookings(accessId, accountId, period));
    }
}
