package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.RawSepaTransactionEntity;
import de.adorsys.multibanking.pers.spi.repository.RawSepaTransactionRepositoryIf;
import de.adorsys.multibanking.repository.RawSepaTransactionRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class RawSepaTransactionRepositoryImpl implements RawSepaTransactionRepositoryIf {

    private final RawSepaTransactionRepositoryMongodb paymentRepository;

    @Override
    public Optional<RawSepaTransactionEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public void save(RawSepaTransactionEntity paymentEntity) {
        paymentRepository.save(paymentEntity);
    }

    @Override
    public void delete(String id) {
        paymentRepository.deleteById(id);
    }

}
