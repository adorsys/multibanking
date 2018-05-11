package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.StandingOrder;
import lombok.Data;

/**
 * Created by alexg on 05.09.17.
 */
@Data
public class StandingOrderEntity extends StandingOrder implements IdentityIf {
    private String id;
    private String accountId;
    private String userId;

}
