package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TransactionAuthorisationRequestTO {

    @ApiModelProperty(value = "SCA authentication data, depending on the chosen authentication method. If the data is" +
        " binary, then it is base64 encoded.",
        required = true)
    private String scaAuthenticationData;

    @ApiModelProperty(value = "The OAuth Token for the process if the SCA method supports it. Otherwise empty.",
        required = true)
    @ToString.Exclude
    private String oauthToken;
}
