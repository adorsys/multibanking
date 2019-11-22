package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "payment_sepa")
@Data
@EqualsAndHashCode(callSuper = false)
public class RawSepaTransactionJpaEntity extends PaymentCommonJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 5000)
    private String painXml;

    private AbstractTransaction.TransactionType sepaTransactionType;

}
