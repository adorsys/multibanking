package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BulkPaymentEntity;
import de.adorsys.multibanking.domain.PaymentEntity;

import java.util.Optional;

/**
 * @author alexg on 04.09.17
 */
public interface BulkPaymentRepositoryIf {

    void save(BulkPaymentEntity target);

    void delete(String id);
}
