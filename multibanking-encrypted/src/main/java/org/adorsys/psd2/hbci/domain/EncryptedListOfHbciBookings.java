package org.adorsys.psd2.hbci.domain;

import org.adorsys.psd2.common.domain.JweEncryptedObject;

import domain.Booking;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="This is the represation of an encrypted List of hbci bookings", value="EncryptedListOfHbciBankAccounts", parent=JweEncryptedObject.class )
public class EncryptedListOfHbciBookings extends JweEncryptedObject {
	private Booking model = new Booking();
	@ApiModelProperty(value = "Descryptive example hbci booking object")
	public Booking getModel() {
		return model;
	}
}
