package de.adorsys.multibanking.mongo.entity;

import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.mongo.encrypt.Encrypted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document
@CompoundIndexes({
        @CompoundIndex(name = "booking_index", def = "{'userId': 1, 'accountId': 1}"),
        @CompoundIndex(name = "booking_unique_index", def = "{'externalId': 1, 'accountId': 1}", unique = true)})
@Encrypted(exclude = {"_id", "accountId", "externalId", "userId", "valutaDate", "bookingDate", "bankApi"})
public class BookingMongoEntity extends Booking {

    @Id
    private String id;
    private String accountId;
    private String userId;

}
