package de.adorsys.multibanking.domain;

import java.math.BigDecimal;

import de.adorsys.multibanking.domain.common.AbstractId;
import lombok.Data;

/**
 * Created by alexg on 01.12.17.
 * TODO fpo 2018-05-06 08:07 - Introduce Creditor IBAN
 * 
 */
@Data
public class AnonymizedBookingEntity extends AbstractId {
    private String creditorId;
    private BigDecimal amount;
    private String purpose;
    private String otherAccountIBAN;
}
