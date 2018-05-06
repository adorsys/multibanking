package de.adorsys.multibanking.web.user;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.domain.UserData;
import de.adorsys.multibanking.exception.InvalidBankAccessException;
import de.adorsys.multibanking.exception.domain.ErrorConstants;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.service.UserDataService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BankAccessBasedController;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author fpo 2018-03-20 11:45
 */
@UserResource
@RestController
@RequestMapping(path = UserDataController.BASE_PATH)
@Api(value = UserDataController.BASE_PATH, 
tags = "MB-001 - User Data", description="Manages information associated with the user. The user data object contains all banking information collected with the exception of bookings.")
public class UserDataController extends BankAccessBasedController {
	public static final String BASE_PATH = BaseController.BASE_PATH;
	
	@Autowired
	private UserDataService uds;
    
	/**
	 * Returns a document containing the last stored and flushed version of user data.
	 * 
	 * @return
	 */
    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    @ApiOperation(value="Load user data", notes = "Loads the user data, containing bank accesses (without credentials), bank accounts and account synchronization preferences.")
    @ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_OK, 
    		message = "Ok", response=UserData.class),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_FORBIDDEN, message = InvalidBankAccessException.MESSAGE_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = ErrorConstants.ERR_HTTP_CODE_BAD_REQUEST_DOC, response = MultibankingError.class)})
    public @ResponseBody ResponseEntity<ByteArrayResource> loadUserData() {
    	return loadBytesForWeb(uds.loadDocument());
    }
    
}
