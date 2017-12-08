package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.domain.BookingEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 01.12.17
 */
public interface AnonymizedBookingRepositoryIf {

    List<AnonymizedBookingEntity> save(List<AnonymizedBookingEntity> bookingEntities);

}
