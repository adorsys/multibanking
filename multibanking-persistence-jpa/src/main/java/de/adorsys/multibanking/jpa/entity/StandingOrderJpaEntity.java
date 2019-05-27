package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.Cycle;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name="payment_periodic")
@Data
public class StandingOrderJpaEntity {

    @Id
    //@GeneratedValue
    private Long id;
    private String accountId;
    private String userId;
    @Lob
    private Object tanSubmitExternal;

    private Cycle cycle;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private BigDecimal amount;
    private String currency;
    @Embedded
    @AttributeOverride(name = "currency", column = @Column(name = "otherAccountCurrency"))
    @AttributeOverride(name = "userId", column = @Column(name = "otherAccountUserId"))
    private BankAccountJpaEntity otherAccount;
    private String usage;
    private boolean delete;

}
