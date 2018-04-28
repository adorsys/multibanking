package de.adorsys.multibanking.domain;

import java.util.Date;

import de.adorsys.multibanking.domain.common.IdentityIf;
import domain.Payment;
import lombok.Data;

/**
 * Created by alexg on 05.09.17.
 */
@Data
public class PaymentEntity extends Payment implements IdentityIf {

    private String id;
    private String userId;
    private Date createdDateTime;
    private String bankAccessId;
    private String bankAccountId;

}
