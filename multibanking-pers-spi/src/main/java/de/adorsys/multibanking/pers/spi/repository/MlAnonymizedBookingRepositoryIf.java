package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;

import java.util.List;
import java.util.Optional;

public interface MlAnonymizedBookingRepositoryIf {
    Optional<MlAnonymizedBookingEntity> findOne(String id);

    List<MlAnonymizedBookingEntity> findByUserId(String userId);

    void save(MlAnonymizedBookingEntity booking);

    boolean exists(String id);

}
