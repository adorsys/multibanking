package de.adorsys.multibanking.jpa.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Data
class PaymentCommonJpaEntity {

    private String userId;
    private Date createdDateTime;
    @JoinColumn(name = "debtor_account_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private BankAccountJpaEntity debtorBankAccount;
    private String orderId;
    private String paymentId;
    private String product;
    @Lob
    private Object tanSubmitExternal;
}
