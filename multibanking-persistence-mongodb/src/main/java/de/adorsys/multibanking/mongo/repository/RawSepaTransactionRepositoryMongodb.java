package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.RawSepaTransactionMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface RawSepaTransactionRepositoryMongodb extends MongoRepository<RawSepaTransactionMongoEntity, String> {

    Optional<RawSepaTransactionMongoEntity> findByUserIdAndId(String userId, String id);

}
