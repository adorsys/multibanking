package de.adorsys.multibanking.domain;

import de.adorsys.multibanking.encrypt.Encrypted;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by alexg on 08.05.17.
 */
@Data
@Document
@Encrypted(exclude = {"_id", "accountId", "userId"})
@CompoundIndexes({
        @CompoundIndex(name = "account_index", def = "{'userId': 1, 'accountId': 1}")
})
public class BookingsIndexEntity {

    @Id
    private String id;
    private String accountId;
    private String userId;

    private Map<String, List<String>> bookingIdSearchList;

    public void updateSearchIndex(List<BookingEntity> bookings) {
        bookingIdSearchList = new HashMap<>();
        bookings.stream()
                .sorted(Collections.reverseOrder())
                .forEach(booking -> {
                    List search = new ArrayList();
                    if (booking.getBookingCategory() != null) {
                        if (StringUtils.hasText(booking.getBookingCategory().getReceiver())) {
                            search.add(booking.getBookingCategory().getReceiver());
                        }
                    }
                    if (booking.getOtherAccount() != null) {
                        if (StringUtils.hasText(booking.getOtherAccount().getName())) {
                            search.add(booking.getOtherAccount().getName());
                        }
                        if (StringUtils.hasText(booking.getOtherAccount().getOwner())) {
                            search.add(booking.getOtherAccount().getOwner());
                        }
                    }
                    bookingIdSearchList.put(booking.getId(), search);
                });
    }
}
