package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BulkPaymentEntity;
import de.adorsys.multibanking.pers.spi.repository.BulkPaymentRepositoryIf;
import de.adorsys.multibanking.repository.BulkPaymentRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"mongo", "fongo"})
@Service
public class BulkPaymentRepositoryImpl implements BulkPaymentRepositoryIf {

    @Autowired
    private BulkPaymentRepositoryMongodb paymentRepository;


    @Override
    public void save(BulkPaymentEntity target) {
        paymentRepository.save(target);
    }
}
