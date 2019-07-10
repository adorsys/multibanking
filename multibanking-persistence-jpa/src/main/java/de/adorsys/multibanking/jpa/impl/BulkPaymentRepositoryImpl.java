package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BulkPaymentEntity;
import de.adorsys.multibanking.jpa.repository.BulkPaymentRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BulkPaymentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BulkPaymentRepositoryImpl implements BulkPaymentRepositoryIf {

    private final BulkPaymentRepositoryJpa paymentRepository;

    @Override
    public void save(BulkPaymentEntity target) {

    }

    @Override
    public void delete(String id) {

    }
}
