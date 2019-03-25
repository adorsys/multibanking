package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BulkPaymentEntity;

/**
 * @author alexg on 04.09.17
 */
public interface BulkPaymentRepositoryIf {

    void save(BulkPaymentEntity target);

    void delete(String id);
}
