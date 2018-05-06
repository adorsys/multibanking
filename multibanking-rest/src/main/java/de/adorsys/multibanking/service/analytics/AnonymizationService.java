package de.adorsys.multibanking.service.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.domain.BookingEntity;

/**
 * Created by alexg on 01.12.17.
 */
@Service
public class AnonymizationService {
	/**
	 * Anonymizes and stores booking in the user object space.
	 * 
	 * @param bookings
	 */
    public List<AnonymizedBookingEntity> anonymizeAndStoreBookingsAsync(List<BookingEntity> bookingEntities) {
            List<BookingEntity> uncategorizedBookings = bookingEntities.stream()
                    .filter(bookingEntity -> bookingEntity.getBookingCategory() == null 
                    		&& (bookingEntity.getCreditorId() != null 
                    			||	(bookingEntity.getOtherAccount()!=null && bookingEntity.getOtherAccount().getIban()!=null)
                    			|| bookingEntity.getUsage()!=null))
                    .collect(Collectors.toList());

            return uncategorizedBookings.stream()
                    .map(bookingEntity -> anonymizeBooking(bookingEntity))
                    .collect(Collectors.toList());
    }

    private AnonymizedBookingEntity anonymizeBooking(BookingEntity bookingEntity) {
        AnonymizedBookingEntity anonymizedBookingEntity = new AnonymizedBookingEntity();
        if (bookingEntity.getAmount().compareTo(BigDecimal.ZERO) == 1) {
            anonymizedBookingEntity.setAmount(new BigDecimal(1));
        } else {
            anonymizedBookingEntity.setAmount(new BigDecimal(-1));
        }
        anonymizedBookingEntity.setCreditorId(bookingEntity.getCreditorId());
        anonymizedBookingEntity.setPurpose(bookingEntity.getUsage());
        anonymizedBookingEntity.setId(bookingEntity.getId());
        if(bookingEntity.getOtherAccount()!=null)
        anonymizedBookingEntity.setOtherAccountIBAN(bookingEntity.getOtherAccount().getIban());

        return anonymizedBookingEntity;
    }
}
