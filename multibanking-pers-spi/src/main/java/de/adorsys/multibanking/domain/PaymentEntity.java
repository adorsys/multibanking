package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import domain.Payment;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "userId"})
public class PaymentEntity extends Payment {

    @Id
    private String id;
    private String userId;
    @Indexed(expireAfterSeconds = 300)
    private Date createdDateTime;

}
