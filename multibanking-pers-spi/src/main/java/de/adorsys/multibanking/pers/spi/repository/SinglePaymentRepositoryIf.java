package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.SinglePaymentEntity;

import java.util.Optional;

/**
 * @author alexg on 04.09.17
 */
public interface SinglePaymentRepositoryIf {

    Optional<SinglePaymentEntity> findByUserIdAndId(String userId, String id);

    void save(SinglePaymentEntity paymentEntity);

    void delete(String id);

}
