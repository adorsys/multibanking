package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(name = "Token request")
@Data
public class TokenRequestTO {

    @NotBlank
    private String authorisationCode;
}
