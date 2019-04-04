package de.adorsys.multibanking.pers.spi.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;

public interface MlAnonymizedBookingRepositoryIf {
	Optional<MlAnonymizedBookingEntity> findOne(String id);

    List<MlAnonymizedBookingEntity> findByUserId(String userId);

    MlAnonymizedBookingEntity save(MlAnonymizedBookingEntity booking);

    boolean exists(String id);

    void deleteById(String id);
    
    boolean deleteByUserId(String userId);
}
