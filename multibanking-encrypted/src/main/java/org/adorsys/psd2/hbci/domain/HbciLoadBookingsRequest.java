package org.adorsys.psd2.hbci.domain;

import domain.BankAccount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="HBCI load accounts request", value="HbciLoadBookingsRequest", parent=HbciBankAccessRequest.class)
public class HbciLoadBookingsRequest extends HbciBankAccessRequest {
	BankAccount bankAccount;

	@ApiModelProperty(value = "The target bank account", required=true)
	public BankAccount getBankAccount() {
		return bankAccount;
	}
	public void setBankAccount(BankAccount bankAccount) {
		this.bankAccount = bankAccount;
	}
}
