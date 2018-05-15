package de.adorsys.multibanking.web.account;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.exception.BankAccessAlreadyExistException;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.UserNotFoundException;
import de.adorsys.multibanking.exception.domain.ErrorConstants;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BankAccessBasedController;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-20 11:45
 */
@UserResource
@RestController
@RequestMapping(path = BankAccessController.BASE_PATH)
@Api(value = BankAccessController.BASE_PATH, 
	tags = "MB-004 - Bank Access", description="Manages information associating a user to an online banking account. Like bank code, bank login and corresponding password. A bank access is part of the user data object. So user the user data endpoint to load all bank accesses of this user.")
public class BankAccessController extends BankAccessBasedController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/bankaccesses"; 
    private final static Logger LOGGER = LoggerFactory.getLogger(BankAccessController.class);

    @RequestMapping(method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value="Create bank access", notes = "Creates and adds a new bank access to the list of bank accesses of this user. Returns the location url for loading the updated user data object.")
    @ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_CREATED, 
    		responseHeaders={@ResponseHeader(name="location", description="Url for loading the updated user data file")},    
    		message = "Provided bank access successfuly created. Reload user data object."),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_PRECONDITION_FAILED, message = UserNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_FORBIDDEN, message = InvalidBankAccessException.MESSAGE_DOC, response = MultibankingError.class),
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = BankAccessAlreadyExistException.MESSAGE_DOC, response = MultibankingError.class),
            @ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = ErrorConstants.ERR_HTTP_CODE_BAD_REQUEST_DOC, response = MultibankingError.class) })
    public HttpEntity<Void> createBankaccess(@ApiParam(name = "bankAccess",  required=true,
    	value = "The bank access data containing (Bank Code, Bank name, Bank login, PIN") 
    	@RequestBody(required = true) BankAccessEntity bankAccess) {
		bankAccessService.createBankAccess(bankAccess);
		// Trigger Perform Services operations.
		LOGGER.debug("Bank access created for " + userId());
		return new ResponseEntity<>(userDataLocationHeader(), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{accessId}", method = RequestMethod.DELETE)
    @ApiOperation(value ="Delete bank access", notes= "Deletes the bank access with the given id, also deletes all associated credentials, accounts and bookings. Returns the location url for loading the updated user data object.")
    @ApiResponses({ 
    		@ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, 
    				responseHeaders={@ResponseHeader(name="location", description="Url for loading the updated user data file")},    		
    				message = "Bank access with provided id successfuly deleted"),
    		@ApiResponse(code = HttpServletResponse.SC_GONE, 
    	    		responseHeaders={@ResponseHeader(name="location", description="Url for loading the updated user data file")},    		
    	    		message = "Bank access with provided id is gone"),
    		@ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_PRECONDITION_FAILED, message = UserNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class)
    })
    public HttpEntity<Void> deleteBankAccess(
    		@ApiParam(name = "accessId",  required=true,
        	value = "The identifier of the bank access to delete.", example="3c149076-13c4-4190-ace3-e30bf8f61526")
    		@PathVariable String accessId) {
        if (bankAccessService.deleteBankAccess(accessId)) {
        	LOGGER.debug("Bank Access [{}] deleted.", accessId);
        	return new ResponseEntity<Void>(userDataLocationHeader(), HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<Void>(HttpStatus.GONE);
        }
    }
}
