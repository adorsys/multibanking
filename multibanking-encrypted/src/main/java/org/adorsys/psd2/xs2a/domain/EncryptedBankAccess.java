package org.adorsys.psd2.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted Bank Access...", value="EncryptedBankAccess" )
public class EncryptedBankAccess {
	private String jweString;
	private BankAccess model = new BankAccess();
	
	@ApiModelProperty(value = "The JWE encrypted value of the bank access object.", required=true)
	public String getJweString() {
		return jweString;
	}
	public void setJweString(String jweString) {
		this.jweString = jweString;
	}

	@ApiModelProperty(value = "Sample bank access object")
	public BankAccess getModel() {
		return model;
	}
	public void setModel(BankAccess model) {
		this.model = model;
	}

}
