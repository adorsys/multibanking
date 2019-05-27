package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.RawSepaTransactionEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.RawSepaTransactionRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.RawSepaTransactionRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class RawSepaTransactionRepositoryImpl implements RawSepaTransactionRepositoryIf {

    private final RawSepaTransactionRepositoryMongodb paymentRepository;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<RawSepaTransactionEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToRawSepaTransactionEntity);
    }

    @Override
    public void save(RawSepaTransactionEntity paymentEntity) {
        paymentRepository.save(entityMapper.mapToRawSepaTransactionMongoEntity(paymentEntity));
    }

    @Override
    public void delete(String id) {
        paymentRepository.deleteById(id);
    }

}
