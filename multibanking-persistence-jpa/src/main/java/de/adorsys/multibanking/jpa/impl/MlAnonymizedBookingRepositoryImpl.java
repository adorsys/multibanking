package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;
import de.adorsys.multibanking.jpa.repository.MlAnonymizedBookingRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.MlAnonymizedBookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class MlAnonymizedBookingRepositoryImpl implements MlAnonymizedBookingRepositoryIf {

    private final MlAnonymizedBookingRepositoryJpa mlAnonymizedBookingRepository;

    @Override
    public Optional<MlAnonymizedBookingEntity> findOne(String id) {
        return Optional.empty();
    }

    @Override
    public List<MlAnonymizedBookingEntity> findByUserId(String userId) {
        return null;
    }

    @Override
    public void save(MlAnonymizedBookingEntity booking) {
    }

    @Override
    public boolean exists(String id) {
        return false;
    }

}
