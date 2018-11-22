package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BulkPaymentEntity;
import de.adorsys.multibanking.domain.PaymentEntity;

import java.util.Optional;

/**
 * @author alexg on 04.09.17
 */
public interface PaymentRepositoryIf {

    Optional<PaymentEntity> findByUserIdAndId(String userId, String id);

    void save(PaymentEntity paymentEntity);

    void delete(String id);

}
