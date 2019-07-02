package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.domain.SinglePayment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name="payment_bulk")
@Data
@EqualsAndHashCode(callSuper = false)
public class BulkPaymentJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    private Date createdDateTime;
    @Lob
    private Object tanSubmitExternal;
    @Embedded
    private List<SinglePayment> payments;

}
