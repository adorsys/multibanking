package de.adorsys.onlinebanking.mock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Carries an access token", value="AccessToken" )
public class AccessToken {
	private String token;

	public AccessToken() {
	}

	public AccessToken(String token) {
		this.token = token;
	}

	@ApiModelProperty(value = "The access token", required=true, example="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNYXhNdXN0ZXJtYW4iLCJyb2xlIjoiVVNFUiIsImV4cCI6MTQ5NTM5MTAxM30.mN9eFMnEuYgh_KCULI8Gpm1X49wWaA67Ps1M7EFV0BQ")
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
