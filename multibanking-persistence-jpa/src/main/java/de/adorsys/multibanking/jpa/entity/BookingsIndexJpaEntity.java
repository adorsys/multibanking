package de.adorsys.multibanking.jpa.entity;

import de.adorsys.multibanking.jpa.converter.BookingSearchMapConverter;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity(name = "bookings_index")
@Data
public class BookingsIndexJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String accountId;
    private String userId;
    @Column(length = 10000)
    @Convert(converter = BookingSearchMapConverter.class)
    private Map<String, List<String>> bookingIdSearchList;
}
