package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptedObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an EncryptedHbciLoadAccountRequest.", value="EncryptedHbciLoadAccountRequest", parent=JweEncryptedObject.class )
public class EncryptedHbciLoadAccountRequest extends JweEncryptedObject{
	private HbciLoadAccountsRequest model = new HbciLoadAccountsRequest();
	@ApiModelProperty(value = "Descriptive example of a HbciLoadAccountRequest object")
	public HbciLoadAccountsRequest getModel() {
		return model;
	}
}
