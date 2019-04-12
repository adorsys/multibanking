package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;
import de.adorsys.multibanking.pers.spi.repository.MlAnonymizedBookingRepositoryIf;
import de.adorsys.multibanking.repository.MlAnonymizedBookingRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class MlAnonymizedBookingRepositoryImpl implements MlAnonymizedBookingRepositoryIf {

    private final MlAnonymizedBookingRepositoryMongodb mlAnonymizedBookingRepository;

    @Override
    public Optional<MlAnonymizedBookingEntity> findOne(String id) {
        return mlAnonymizedBookingRepository.findById(id);
    }

    @Override
    public List<MlAnonymizedBookingEntity> findByUserId(String userId) {
        return mlAnonymizedBookingRepository.findByUserId(userId);
    }

    @Override
    public MlAnonymizedBookingEntity save(MlAnonymizedBookingEntity booking) {
        return mlAnonymizedBookingRepository.save(booking);
    }

    @Override
    public boolean exists(String id) {
        return mlAnonymizedBookingRepository.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        mlAnonymizedBookingRepository.deleteById(id);
    }

    @Override
    public boolean deleteByUserId(String userId) {
        return mlAnonymizedBookingRepository.deleteByUserId(userId) > 0;
    }

}
