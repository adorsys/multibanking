package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountReferenceTO {

    @ApiModelProperty(value = "account iban", required = true)
    private String iban;
    private String currency;
}
