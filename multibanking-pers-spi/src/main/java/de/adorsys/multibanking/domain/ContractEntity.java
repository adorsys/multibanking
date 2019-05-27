package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ContractEntity extends Contract {

    private String id;
    private String userId;
    private String accountId;

}
