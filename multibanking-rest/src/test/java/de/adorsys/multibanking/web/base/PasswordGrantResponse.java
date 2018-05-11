package de.adorsys.multibanking.web.base;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PasswordGrantResponse {
// {"access_token":"eyJraWQiOiJtdWx0aWJhbmtpbmctMjM2ZDczOWUtNzk1Ny00YzJiLWIzMDAtN2NmZmRmZWU2ODc4IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJmcmFuY2lzIiwibmJmIjoxNTI0NTM1NDQwLCJyb2xlIjoiVVNFUiIsImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo1MjQ1MCIsInR5cCI6IkJlYXJlciIsImV4cCI6MTUyNDUzNTc0MCwiaWF0IjoxNTI0NTM1NDQwLCJqdGkiOiI3NTgxYmJkZC0wZGMwLTQ0ZTgtYTgwYi0yOWM0OWM0ZGU5MmMifQ.BFcbUQ5bFS2SQnWkjQ-zAAsf2IgJgmN25xm7XgTTKxeR3Fi3N590dxwSz-l2WgYVK4ZAUMMOg3e5zUnYYd-q7E-fzsGftGcc3qEoIJZYNOCe-_6Y1klcFwENqsuQ10J34Uj3mks-yfxgKMJ_DAQjKxcevlwe1lZaPjKF8czf5_WDT9qxeGkyJYvxnqrflztXudOjYpH_5SGRTFWASf6DmQiwDL0SH3hRQd-4CizDKKgF4KNwyc2P-lNuJhV5NFNDAwZeQnsD4o6rW_J8SfSmtEThu7OYIH5Q_h3hpETUg_7IkaDwR2A83vfOHtHv1SwQNURc9lPQQVlggb-qvS56VQ",
//"issued_token_type":"urn:ietf:params:oauth:token-type:access_token",
// "token_type":"Bearer",
// "expires_in":299,
// "scope":null,
// "refresh_token":null}
	
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
