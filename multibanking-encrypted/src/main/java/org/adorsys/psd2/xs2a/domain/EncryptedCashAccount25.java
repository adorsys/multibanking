package org.adorsys.psd2.xs2a.domain;

import org.adorsys.psd2.iso20022.camt053.CashAccount25;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted cash account", value="EncryptedCashAccount25" )
public class EncryptedCashAccount25 {
	private String jweString;
	private CashAccount25 model;
	
	@ApiModelProperty(value = "The JWE encrypted value of the cash account.", required=true)
	public String getJweString() {
		return jweString;
	}
	public void setJweString(String jweString) {
		this.jweString = jweString;
	}

	@ApiModelProperty(value = "Sample cash account object")
	public CashAccount25 getModel() {
		return model;
	}
	public void setModel(CashAccount25 model) {
		this.model = model;
	}

}
