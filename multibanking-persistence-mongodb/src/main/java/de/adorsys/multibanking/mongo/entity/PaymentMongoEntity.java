package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.transaction.SinglePayment;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@Encrypted(exclude = {"_id", "userId"})
public class PaymentMongoEntity extends SinglePayment {

    @Id
    private String id;
    private String userId;
    @Indexed(expireAfterSeconds = 300)
    private Date createdDateTime;
    private Object tanSubmitExternal;

}
