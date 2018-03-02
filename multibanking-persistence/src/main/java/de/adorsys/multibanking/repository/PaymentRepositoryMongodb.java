package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.PaymentEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface PaymentRepositoryMongodb extends MongoRepository<PaymentEntity, String> {

    Optional<PaymentEntity> findByUserIdAndId(String userId, String id);


}
