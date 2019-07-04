package de.adorsys.multibanking.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "payment_single")
@Data
@EqualsAndHashCode(callSuper = false)
public class SinglePaymentJpaEntity extends PaymentCommonJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String receiver;
    private String receiverBic;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String receiverAccountCurrency;
    private String purpose;
    private BigDecimal amount;
    private String currency;

}
