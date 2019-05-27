package de.adorsys.multibanking.jpa.entity;

import lombok.Data;
import org.springframework.util.StringUtils;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.*;

@Entity(name="bookings_index")
@Data
public class BookingsIndexJpaEntity {

    @Id
    //@GeneratedValue
    private Long id;
    private String accountId;
    private String userId;
    @Embedded
    private Map<Long, List<String>> bookingIdSearchList;

    public void updateSearchIndex(List<BookingJpaEntity> bookings) {
        bookingIdSearchList = new HashMap<>();
        new LinkedList<>(bookings)
                .descendingIterator()
                .forEachRemaining(booking -> {
                    List<String> search = new ArrayList<>();
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
