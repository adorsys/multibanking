package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.RawSepaTransactionEntity;

import java.util.Optional;

/**
 * @author alexg on 04.09.17
 */
public interface RawSepaTransactionRepositoryIf {

    Optional<RawSepaTransactionEntity> findByUserIdAndId(String userId, String id);

    void save(RawSepaTransactionEntity paymentEntity);

    void delete(String id);

}
