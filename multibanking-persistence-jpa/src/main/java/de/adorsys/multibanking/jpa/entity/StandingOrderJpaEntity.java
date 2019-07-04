package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.Cycle;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "payment_periodic")
@Data
public class StandingOrderJpaEntity extends PaymentCommonJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String accountId;
    private Cycle cycle;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private BigDecimal amount;
    private String currency;
    @Embedded
    @AttributeOverride(name = "currency", column = @Column(name = "otherAccountCurrency"))
    private BankAccountCommonJpaEntity otherAccount;
    private String usage;
    private boolean delete;

}
