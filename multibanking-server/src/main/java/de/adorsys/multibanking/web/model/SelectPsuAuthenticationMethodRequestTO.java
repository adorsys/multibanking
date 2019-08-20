package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SelectPsuAuthenticationMethodRequestTO {
    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the " +
        "authentication method selection.",
        required = true,
        example = "myAuthenticationID"
    )
    private String authenticationMethodId;
}
