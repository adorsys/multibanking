package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SelectPsuAuthenticationMethodRequestTO {
    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the " +
        "authentication method selection.",
        required = true,
        example = "myAuthenticationID"
    )
    @NotBlank
    private String authenticationMethodId;
    @ApiModelProperty(value = "TAN media name",
        example = "mT:015154423072"
    )
    private String tanMediaName;
}
