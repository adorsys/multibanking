package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptedObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted HbciLoadBookingsRequest.", value="EncryptedHbciLoadBookingsRequest", parent=JweEncryptedObject.class )
public class EncryptedHbciLoadBookingsRequest extends JweEncryptedObject{
	private HbciLoadBookingsRequest model = new HbciLoadBookingsRequest();
	@ApiModelProperty(value = "Descriptive example of a HbciLoadBookingsRequest object")
	public HbciLoadBookingsRequest getModel() {
		return model;
	}
}
