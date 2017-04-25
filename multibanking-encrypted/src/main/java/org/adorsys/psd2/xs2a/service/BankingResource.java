package org.adorsys.psd2.xs2a.service;

import org.adorsys.psd2.xs2a.domain.EncryptedAccountReport;
import org.adorsys.psd2.xs2a.domain.EncryptedAccountStatement;
import org.adorsys.psd2.xs2a.domain.EncryptedBankAccess;
import org.adorsys.psd2.xs2a.domain.EncryptedCashAccount25;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;

@Api(value = "/v1/xs2a", tags={"Access To Account ISO20022"}, authorizations = @Authorization(value = "BearerToken"), description = "The search service allows to retrieve results from the index. It comes with methods to execute a search, and to receive static metadata needed to display the search results. All method are ex- isting in two variants: one including user authentication, and another one requesting a valid ac- cess token.")
public class BankingResource {

	@ApiOperation(value = "accounts", notes = "Load all bank accounts associated with given baking acess data")
	@ApiResponses(value = { @ApiResponse (code = 200, message = "Ok", response=EncryptedAccountReport.class),
			@ApiResponse(code = 400, message = "Bad request", responseHeaders=@ResponseHeader(name="ERROR_KEY", description="BAD_REQUEST"))})
	public EncryptedAccountReport loadBankAccounts(@ApiParam(value="The encrypted bank access object") EncryptedBankAccess bankAccess) {
		return new EncryptedAccountReport();
	}

	@ApiOperation(value = "Load statement", notes = "Load statement associated with given bank account")
	@ApiResponses(value = { @ApiResponse (code = 200, message = "Ok", response=EncryptedAccountStatement.class),
			@ApiResponse(code = 400, message = "Bad request", responseHeaders=@ResponseHeader(name="ERROR_KEY", description="BAD_REQUEST"))})
	public EncryptedAccountStatement loadPostings(@ApiParam(value="The encrypted bank access object")EncryptedBankAccess bankAccess, 
			@ApiParam(value="The encrypted cash account") EncryptedCashAccount25 cashAccount) {
		return new EncryptedAccountStatement();
	}
}
