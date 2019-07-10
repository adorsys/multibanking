package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.RawSepaTransactionEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.RawSepaTransactionRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.RawSepaTransactionRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class RawSepaTransactionRepositoryImpl implements RawSepaTransactionRepositoryIf {

    private final RawSepaTransactionRepositoryJpa paymentRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public Optional<RawSepaTransactionEntity> findByUserIdAndId(String userId, String id) {
        return paymentRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToRawSepaTransactionEntity);
    }

    @Override
    public void save(RawSepaTransactionEntity paymentEntity) {

    }

    @Override
    public void delete(String id) {

    }

}
