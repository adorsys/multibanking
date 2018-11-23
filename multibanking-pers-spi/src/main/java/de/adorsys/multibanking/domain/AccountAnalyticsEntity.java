package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import domain.BookingGroup;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by alexg on 08.05.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId", "analyticsDate"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class AccountAnalyticsEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;

    private LocalDateTime analyticsDate = LocalDateTime.now().now();

    private List<BookingGroup> bookingGroups;

}
