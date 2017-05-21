package de.adorsys.multibanking.pers.jcloud.domain;

import java.util.List;

import de.adorsys.multibanking.domain.BookingEntity;
import lombok.Data;

@Data
public class UserBookingRecord {
	private List<BookingEntity> bookings;
}
