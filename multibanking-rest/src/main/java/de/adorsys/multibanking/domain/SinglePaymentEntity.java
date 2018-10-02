package de.adorsys.multibanking.domain;

import java.util.Date;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.SinglePayment;
import lombok.Data;

/**
 * Created by alexg on 05.09.17.
 */
@Data
public class SinglePaymentEntity extends SinglePayment implements IdentityIf {

    private String id;
    private String userId;
    private Date createdDateTime;
    private String bankAccessId;
    private String bankAccountId;
    private Object tanSubmitExternal;

}
