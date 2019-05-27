package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class RawSepaTransactionEntity extends RawSepaPayment {

    private String id;
    private String userId;
    private Date createdDateTime;
    private Object tanSubmitExternal;

}
