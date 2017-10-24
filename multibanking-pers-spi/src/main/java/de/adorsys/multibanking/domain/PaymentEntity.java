package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import domain.Payment;
import domain.StandingOrder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alexg on 05.09.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId"})
@CompoundIndexes({
        @CompoundIndex(name = "payment_index", def = "{'userId': 1, 'accountId': 1}")
})
public class PaymentEntity extends Payment {

    @Id
    private String id;
    private String accountId;
    private String userId;

}
