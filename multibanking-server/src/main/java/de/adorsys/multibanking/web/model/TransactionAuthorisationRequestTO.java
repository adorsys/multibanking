package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
public class TransactionAuthorisationRequestTO {
    @ApiModelProperty(value = "SCA authentication data, depending on the chosen authentication method. If the data is" +
        " binary, then it is base64 encoded.",
        required = true)
    @ToString.Exclude
    private String scaAuthenticationData;
}
