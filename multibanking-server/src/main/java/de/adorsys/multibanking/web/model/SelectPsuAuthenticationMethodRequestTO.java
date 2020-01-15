package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(name = "SelectScaMethod")
@Data
public class SelectPsuAuthenticationMethodRequestTO {
    @Schema(description = "An identification provided by the ASPSP for the later identification of the " +
        "authentication method selection.",
        required = true,
        example = "myAuthenticationID"
    )
    @NotBlank
    private String authenticationMethodId;
    @Schema(description = "TAN media name",
        example = "mT:015154423072"
    )
    private String tanMediaName;
}
