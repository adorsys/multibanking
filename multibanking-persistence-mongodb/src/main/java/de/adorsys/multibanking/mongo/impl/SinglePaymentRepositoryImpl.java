package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.SinglePaymentEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.PaymentRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.SinglePaymentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class SinglePaymentRepositoryImpl implements SinglePaymentRepositoryIf {

    private final PaymentRepositoryMongodb paymentRepository;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<SinglePaymentEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToPaymentEntity);
    }

    @Override
    public void save(SinglePaymentEntity paymentEntity) {
        paymentRepository.save(entityMapper.mapToPaymentMongoEntity(paymentEntity));
    }

    @Override
    public void delete(String id) {
        paymentRepository.deleteById(id);
    }

}
