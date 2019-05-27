package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.PaymentMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface PaymentRepositoryMongodb extends MongoRepository<PaymentMongoEntity, String> {

    Optional<PaymentMongoEntity> findByUserIdAndId(String userId, String id);

}
