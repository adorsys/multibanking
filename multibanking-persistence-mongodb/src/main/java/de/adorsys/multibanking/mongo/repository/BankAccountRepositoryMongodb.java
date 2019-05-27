package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.BankAccountMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface BankAccountRepositoryMongodb extends MongoRepository<BankAccountMongoEntity, String> {

    List<BankAccountMongoEntity> findByUserId(String userId);

    List<BankAccountMongoEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId);

    Optional<BankAccountMongoEntity> findByUserIdAndId(String userId, String id);

    List<BankAccountMongoEntity> deleteByBankAccessId(String accessId);
}
