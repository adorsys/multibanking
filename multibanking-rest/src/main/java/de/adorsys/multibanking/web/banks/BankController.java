package de.adorsys.multibanking.web.banks;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibanking.exception.UserNotFoundException;
import de.adorsys.multibanking.exception.domain.ErrorConstants;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.service.BankService;
import de.adorsys.multibanking.web.annotation.UserResource;
import de.adorsys.multibanking.web.common.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author alexg on 07.02.17.
 * @author fpo 2018-03-20 11:45
 */
@UserResource
@RestController
@RequestMapping(path = BankController.BASE_PATH)
@Api(value = BankController.BASE_PATH, 
	tags = "MB-003 - Banks", description="Loads the list of banks supported by this application. Clients might user this to offer search.")
public class BankController extends BaseController {
	public static final String BASE_PATH = BaseController.BASE_PATH + "/bank"; 

	@Autowired
	BankService bankService;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    @ApiOperation(value = "Loads and returns the list of supported banks.")
    @ApiResponses(value = { @ApiResponse(code = HttpServletResponse.SC_OK, message = "Ok"),
            @ApiResponse(code = HttpServletResponse.SC_UNAUTHORIZED, message = ErrorConstants.ERR_HTTP_CODE_UNAUTHENTICATED_DOC, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_PRECONDITION_FAILED, message = UserNotFoundException.RENDERED_MESSAGE_KEY, response = MultibankingError.class),
    		@ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = ErrorConstants.ERR_HTTP_CODE_BAD_REQUEST_DOC, response = MultibankingError.class)})
	public  ResponseEntity<ByteArrayResource> loadBanks() {
		return loadBytesForWeb(bankService.loadDocument(), MediaType.APPLICATION_JSON_UTF8);
	}
}
