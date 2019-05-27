package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.StandingOrderMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"mongo", "fongo"})
public interface StandingOrderRepositoryMongodb extends MongoRepository<StandingOrderMongoEntity, String> {

    List<StandingOrderMongoEntity> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByAccountId(String accountId);

}
