package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class StandingOrderEntity extends StandingOrder {

    private String id;
    private String accountId;
    private String userId;
    private Object tanSubmitExternal;

}
