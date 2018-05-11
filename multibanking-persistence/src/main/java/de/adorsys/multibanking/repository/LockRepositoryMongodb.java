package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.LockEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Profile({"mongo", "fongo"})
@Repository
public interface LockRepositoryMongodb extends MongoRepository<LockEntity, String> {
    LockEntity findByName(String name);

    LockEntity findByNameAndValue(String name, String value);

    void deleteByName(String name);
}