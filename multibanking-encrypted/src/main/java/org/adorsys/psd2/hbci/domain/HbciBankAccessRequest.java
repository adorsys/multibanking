package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptionSpec;

import domain.BankAccess;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="HBCI bank access request", value="HbciBankAccessRequest", parent=JweEncryptionSpec.class)
public class HbciBankAccessRequest extends JweEncryptionSpec {
	private BankAccess bankAccess;
	private String pin;
	
	@ApiModelProperty(value = "The user accounts access data", required=true)
	public BankAccess getBankAccess() {
		return bankAccess;
	}
	public void setBankAccess(BankAccess bankAccess) {
		this.bankAccess = bankAccess;
	}

	@ApiModelProperty(value = "This is the user personal identification number or password.", required=true)
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
}
