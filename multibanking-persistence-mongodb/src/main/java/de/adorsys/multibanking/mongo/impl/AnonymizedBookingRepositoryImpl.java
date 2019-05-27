package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.AnonymizedBookingRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.AnonymizedBookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class AnonymizedBookingRepositoryImpl implements AnonymizedBookingRepositoryIf {

    private final AnonymizedBookingRepositoryMongodb anonymizdBookingRepository;
    private final MongoEntityMapper entityMapper;

    @Override
    public void save(List<AnonymizedBookingEntity> bookingEntities) {
        anonymizdBookingRepository.saveAll(entityMapper.mapToAnonymizedBookingMongoEntities(bookingEntities));
    }
}
