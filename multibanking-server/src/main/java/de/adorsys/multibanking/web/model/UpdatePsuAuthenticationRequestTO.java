package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePsuAuthenticationRequestTO {

    @NotBlank
    @ApiModelProperty(value = "Password", required = true)
    private String psuId;
    private String psuCorporateId;

    @NotBlank
    @ApiModelProperty(value = "Password", required = true)
    @ToString.Exclude
    private String password;
}
