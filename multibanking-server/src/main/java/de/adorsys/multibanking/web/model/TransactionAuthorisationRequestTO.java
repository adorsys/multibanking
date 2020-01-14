package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "SCA authentication request")
@Data
public class TransactionAuthorisationRequestTO {

    @Schema(description = "SCA authentication data, depending on the chosen authentication method. If the data is" +
        " binary, then it is base64 encoded.",
        required = true)
    private String scaAuthenticationData;

}
