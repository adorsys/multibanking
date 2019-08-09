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
    private BankApi bankApi;

}
