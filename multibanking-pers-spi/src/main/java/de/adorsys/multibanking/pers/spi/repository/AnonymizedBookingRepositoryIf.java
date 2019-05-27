package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;

import java.util.List;

public interface AnonymizedBookingRepositoryIf {

    void save(List<AnonymizedBookingEntity> bookingEntities);

}
