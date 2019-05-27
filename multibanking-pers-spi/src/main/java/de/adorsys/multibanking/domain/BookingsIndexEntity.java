package de.adorsys.multibanking.domain;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public class BookingsIndexEntity {

    private String id;
    private String accountId;
    private String userId;

    private Map<String, List<String>> bookingIdSearchList;

    public void updateSearchIndex(List<BookingEntity> bookings) {
        bookingIdSearchList = new HashMap<>();
        new LinkedList<>(bookings)
                .descendingIterator()
                .forEachRemaining(booking -> {
                    List<String> search = new ArrayList<>();
                    if (booking.getBookingCategory() != null) {
                        if (StringUtils.isNotBlank(booking.getBookingCategory().getReceiver())) {
                            search.add(booking.getBookingCategory().getReceiver());
                        }
                    }
                    if (booking.getOtherAccount() != null) {
                        if (StringUtils.isNotBlank(booking.getOtherAccount().getName())) {
                            search.add(booking.getOtherAccount().getName());
                        }
                        if (StringUtils.isNotBlank(booking.getOtherAccount().getOwner())) {
                            search.add(booking.getOtherAccount().getOwner());
                        }
                    }
                    bookingIdSearchList.put(booking.getId(), search);
                });
    }
}
