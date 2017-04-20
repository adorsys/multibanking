package org.adorsys.psd2.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Holds bank account access data.", value="BankAccess" )
public class BankAccess {

	private String bankLogin;
    
	private String bankCode;
    
	private String country;
    
	private String pin;
    
	private String bic;
    
	@ApiModelProperty(value = "This is the user's bank login. Some bank require the user bank account number. Oder require the a predefined login", required=true)
	public String getBankLogin() {
		return bankLogin;
	}
	public void setBankLogin(String bankLogin) {
		this.bankLogin = bankLogin;
	}

	@ApiModelProperty(value = "The is the country local code of the bank.", required=false)
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	
	@ApiModelProperty(value = "This is the country ISO2 code. Needed when user specify the bankCode instead of the bic", required=false)
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	@ApiModelProperty(value = "The bank identification code. Required when the user does not specify the bank code. ", required=false)
	public String getBic() {
		return bic;
	}
	public void setBic(String bic) {
		this.bic = bic;
	}
	@ApiModelProperty(value = "This is the user personal identification number or password.", required=true)
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
    
}
