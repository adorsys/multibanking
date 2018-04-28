package de.adorsys.multibanking.domain;

import java.math.BigDecimal;

import de.adorsys.multibanking.domain.common.AbstractId;
import lombok.Data;

/**
 * Created by alexg on 01.12.17.
 */
@Data
public class AnonymizedBookingEntity extends AbstractId {
    private String creditorId;
    private BigDecimal amount;
    private String purpose;
}
