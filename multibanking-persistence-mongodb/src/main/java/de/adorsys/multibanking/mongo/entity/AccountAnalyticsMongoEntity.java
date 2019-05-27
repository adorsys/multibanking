package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.BookingGroup;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId", "analyticsDate"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class AccountAnalyticsMongoEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;
    private LocalDateTime analyticsDate = LocalDateTime.now();
    private List<BookingGroup> bookingGroups;

}
