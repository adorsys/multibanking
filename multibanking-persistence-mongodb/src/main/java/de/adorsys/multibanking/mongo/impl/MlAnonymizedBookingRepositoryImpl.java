package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.MlAnonymizedBookingRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.MlAnonymizedBookingRepositoryIf;
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
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<MlAnonymizedBookingEntity> findOne(String id) {
        return mlAnonymizedBookingRepository.findById(id)
                .map(entityMapper::mapToMlAnonymizedBookingEntity);
    }

    @Override
    public List<MlAnonymizedBookingEntity> findByUserId(String userId) {
        return entityMapper.mapToMlAnonymizedBookingEntities(mlAnonymizedBookingRepository.findByUserId(userId));
    }

    @Override
    public void save(MlAnonymizedBookingEntity booking) {
        mlAnonymizedBookingRepository.save(entityMapper.mapToMlAnonymizedBookingMongoEntity(booking));
    }

    @Override
    public boolean exists(String id) {
        return mlAnonymizedBookingRepository.existsById(id);
    }

}
