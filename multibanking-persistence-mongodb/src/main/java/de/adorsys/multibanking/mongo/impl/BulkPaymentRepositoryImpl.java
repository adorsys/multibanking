package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BulkPaymentEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BulkPaymentRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BulkPaymentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BulkPaymentRepositoryImpl implements BulkPaymentRepositoryIf {

    private final BulkPaymentRepositoryMongodb paymentRepository;
    private final MongoEntityMapper entityMapper;

    @Override
    public void save(BulkPaymentEntity target) {
        paymentRepository.save(entityMapper.mapToBulkPaymentMongoEntity(target));
    }

    @Override
    public void delete(String id) {
        paymentRepository.deleteById(id);
    }
}
