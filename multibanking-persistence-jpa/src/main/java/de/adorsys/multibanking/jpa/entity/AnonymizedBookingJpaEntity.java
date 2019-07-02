package de.adorsys.multibanking.jpa.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity(name="anonymized_booking")
public class AnonymizedBookingJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String creditorId;
    private BigDecimal amount;
    private String purpose;
}
