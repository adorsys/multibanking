package de.adorsys.multibanking.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountReferenceTO {

    private String iban;
    private String currency;
}
