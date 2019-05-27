package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class BookingsIndexMongoEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;

    private Map<String, List<String>> bookingIdSearchList;
}
