package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.AccountAnalyticsMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface AnalyticsRepositoryMongodb extends MongoRepository<AccountAnalyticsMongoEntity, String> {

    Optional<AccountAnalyticsMongoEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId);

    void deleteByAccountId(String id);

}
