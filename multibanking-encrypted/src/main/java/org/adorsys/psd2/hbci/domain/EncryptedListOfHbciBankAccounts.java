package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptedObject;

import domain.BankAccount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted List of hbci bank accounts", value="EncryptedListOfHbciBankAccounts", parent=JweEncryptedObject.class )
public class EncryptedListOfHbciBankAccounts extends JweEncryptedObject {
	private BankAccount model = new BankAccount();
	@ApiModelProperty(value = "Descryptive example hbci bank account object")
	public BankAccount getModel() {
		return model;
	}
}
