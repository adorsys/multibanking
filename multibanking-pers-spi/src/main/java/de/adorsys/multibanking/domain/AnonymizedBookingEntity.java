package de.adorsys.multibanking.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnonymizedBookingEntity {

    private String id;
    private String creditorId;
    private BigDecimal amount;
    private String purpose;
}
