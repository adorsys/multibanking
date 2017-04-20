package org.adorsys.psd2.common.domain;

import com.nimbusds.jose.jwk.JWK;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Contains encyption parameters", value="JweEncryptionSpec" )
public class JweEncryptionSpec {
	private String algo;
	private String enc;
	private JWK key;
	
	@ApiModelProperty(value = "The response encryption algorithm. For example RSA-OAEP or DIR", required=true)
	public String getAlgo() {
		return algo;
	}
	public void setAlgo(String algo) {
		this.algo = algo;
	}
	@ApiModelProperty(value = "The response encryption method. FOr example A128CBC-HS256 or A256GCM", required=true)
	public String getEnc() {
		return enc;
	}
	public void setEnc(String enc) {
		this.enc = enc;
	}
	@ApiModelProperty(value = "The response encryption key in jwk format.", required=true)
	public JWK getKey() {
		return key;
	}
	public void setKey(JWK key) {
		this.key = key;
	}
}
