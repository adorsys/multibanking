package de.adorsys.multibanking.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BookingEntity extends Booking {

    private String id;
    private String accountId;
    private String userId;

}
