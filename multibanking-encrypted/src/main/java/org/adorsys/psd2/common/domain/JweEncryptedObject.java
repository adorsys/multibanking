package org.adorsys.psd2.common.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Represents a jwe encrypted object", value="JweEncryptedObject" )
public class JweEncryptedObject {
	private String jweString;

	@ApiModelProperty(value = "The JWE string containing the encrypted object.", required=true)
	public String getJweString() {
		return jweString;
	}
	public void setJweString(String jweString) {
		this.jweString = jweString;
	}
}
