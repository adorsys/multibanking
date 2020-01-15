package de.adorsys.multibanking.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConsentEntity {

    private String id;
    private String authorisationId;
    private String redirectId;
    private boolean temporary;
    private BankApi bankApi;
    private String psuAccountIban;
    private Object bankApiConsentData;
}
