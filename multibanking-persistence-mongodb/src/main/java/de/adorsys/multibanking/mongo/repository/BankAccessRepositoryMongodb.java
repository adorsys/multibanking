package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.BankAccessMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface BankAccessRepositoryMongodb extends MongoRepository<BankAccessMongoEntity, String> {

    Optional<BankAccessMongoEntity> findByUserIdAndId(String userId, String id);

    List<BankAccessMongoEntity> findByUserId(String userId);

    long deleteByUserIdAndId(String userId, String id);

    List<BankAccessMongoEntity> findByUserIdAndConsentId(String userId);

}
