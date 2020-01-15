package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(name = "PsuAuthentication")
@Data
public class UpdatePsuAuthenticationRequestTO {

    @NotBlank
    @Schema(description = "Password", required = true)
    private String psuId;
    private String psuCorporateId;

    @NotBlank
    @Schema(description = "Password", required = true)
    private String password;
}
