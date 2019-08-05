package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.transaction.StandingOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class StandingOrderEntity extends StandingOrder {

    private String id;
    private String accountId;
    private String userId;
    private Date createdDateTime;
    private Object tanSubmitExternal;

}
