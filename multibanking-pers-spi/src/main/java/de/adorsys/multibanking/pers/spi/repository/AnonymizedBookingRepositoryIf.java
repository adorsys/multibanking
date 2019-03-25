package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;

import java.util.List;

/**
 * @author alexg on 01.12.17
 */
public interface AnonymizedBookingRepositoryIf {

    List<AnonymizedBookingEntity> save(List<AnonymizedBookingEntity> bookingEntities);

}
