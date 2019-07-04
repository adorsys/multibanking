package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.SinglePaymentEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.PaymentRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.SinglePaymentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class SinglePaymentRepositoryImpl implements SinglePaymentRepositoryIf {

    private final PaymentRepositoryJpa paymentRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public Optional<SinglePaymentEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToPaymentEntity);
    }

    @Override
    public void save(SinglePaymentEntity paymentEntity) {

    }

    @Override
    public void delete(String id) {

    }

}
