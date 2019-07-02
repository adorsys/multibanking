package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.BankApi;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "booking")
@Data
@EqualsAndHashCode(callSuper = false)
public class BookingJpaEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;
    private String externalId;

    @AttributeOverride(name = "currency", column = @Column(name = "otherAccountCurrency"))
    @Embedded
    private BankAccountCommonJpaEntity otherAccount;
    private LocalDate valutaDate;
    private LocalDate bookingDate;
    private BigDecimal amount;
    private String currency;
    private boolean reversal;
    private BigDecimal balance;
    private String customerRef;
    private String instRef;
    private BigDecimal origValue;
    private BigDecimal chargeValue;
    private String text;
    private String additional;
    private String primanota;
    private String usage;
    private String addkey;
    private boolean sepa;
    private boolean standingOrder;
    private String creditorId;
    private String mandateReference;
    private BankApi bankApi;
    @Embedded
    private BookingCategoryJpaEntity bookingCategory;
    private String transactionCode;

}
