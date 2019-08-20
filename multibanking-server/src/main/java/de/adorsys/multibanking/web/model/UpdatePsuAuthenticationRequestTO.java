package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdatePsuAuthenticationRequestTO {

    private String psuId;

    private String psuCustomerId;

    @ApiModelProperty(value = "Password", required = true)
    private String password;
}
