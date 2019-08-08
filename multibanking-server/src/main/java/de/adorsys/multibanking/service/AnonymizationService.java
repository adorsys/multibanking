package de.adorsys.multibanking.service;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.pers.spi.repository.AnonymizedBookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
class AnonymizationService {

    private final AnonymizedBookingRepositoryIf anonymizedBookingRepository;

    @Async
    void anonymizeAndStoreBookingsAsync(List<BookingEntity> bookingEntities) {
        List<BookingEntity> uncategorizedBookings = bookingEntities.stream()
            .filter(bookingEntity -> bookingEntity.getBookingCategory() == null && bookingEntity.getCreditorId() != null)
            .collect(Collectors.toList());

        List<AnonymizedBookingEntity> anonymizedBookings = uncategorizedBookings.stream()
            .map(this::anonymizeBooking)
            .collect(Collectors.toList());

        try {
            anonymizedBookingRepository.save(anonymizedBookings);
        } catch (DuplicateKeyException e) {
            //ignore it
        }
    }

    private AnonymizedBookingEntity anonymizeBooking(BookingEntity bookingEntity) {
        AnonymizedBookingEntity anonymizedBookingEntity = new AnonymizedBookingEntity();
        if (bookingEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            anonymizedBookingEntity.setAmount(new BigDecimal(1));
        } else {
            anonymizedBookingEntity.setAmount(new BigDecimal(-1));
        }
        anonymizedBookingEntity.setCreditorId(bookingEntity.getCreditorId());
        anonymizedBookingEntity.setPurpose(bookingEntity.getUsage());

        return anonymizedBookingEntity;
    }
}
