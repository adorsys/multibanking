package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.PaymentEntity;
import de.adorsys.multibanking.pers.spi.repository.SinglePaymentRepositoryIf;
import de.adorsys.multibanking.repository.PaymentRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class SinglePaymentRepositoryImpl implements SinglePaymentRepositoryIf {

    @Autowired
    private PaymentRepositoryMongodb paymentRepository;

    @Override
    public Optional<PaymentEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public void save(PaymentEntity paymentEntity) {
        paymentRepository.save(paymentEntity);
    }

    @Override
    public void delete(String id) {
        paymentRepository.deleteById(id);
    }

}
