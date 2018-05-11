package de.adorsys.mbs.authserver.example.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PasswordGrantResponse {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("issued_token_type")
	private String issuedTokenType;
	@JsonProperty("expires_in")
	private int expiresIn;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("refresh_token")
	private String refreshToken;
}
