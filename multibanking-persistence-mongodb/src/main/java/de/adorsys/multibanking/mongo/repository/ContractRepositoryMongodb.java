package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.ContractMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"mongo", "fongo"})
public interface ContractRepositoryMongodb extends MongoRepository<ContractMongoEntity, String> {

    List<ContractMongoEntity> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByAccountId(String accountId);

}
