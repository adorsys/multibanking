package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.BankMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface BankRepositoryMongodb extends MongoRepository<BankMongoEntity, String> {

    Optional<BankMongoEntity> findByBankCode(String bankCode);
}
