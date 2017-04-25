package org.adorsys.psd2.xs2a.domain;

import org.adorsys.psd2.iso20022.camt053.BankToCustomerStatementV06;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted account statement...", value="EncryptedAccountStatement" )
public class EncryptedAccountStatement {
	private String jweString;
	private BankToCustomerStatementV06 model;
	
	@ApiModelProperty(value = "The JWE encrypted value of the account statement object.", required=true)
	public String getJweString() {
		return jweString;
	}
	public void setJweString(String jweString) {
		this.jweString = jweString;
	}
	@ApiModelProperty(value = "Sample account statement object")
	public BankToCustomerStatementV06 getModel() {
		return model;
	}
	public void setModel(BankToCustomerStatementV06 model) {
		this.model = model;
	}

}
