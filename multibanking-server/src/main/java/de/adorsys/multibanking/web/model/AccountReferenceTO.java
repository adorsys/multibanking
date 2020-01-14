package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Account reference")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountReferenceTO {

    @Schema(description = "account iban", required = true)
    private String iban;
    private String currency;
}
