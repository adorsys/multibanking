package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.KeyStoreEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Profile({"mongo", "fongo"})
@Repository
public interface KeyStoreRepositoryMongodb extends MongoRepository<KeyStoreEntity, String> {

    KeyStoreEntity findByName(String name);

    long countByName(String name);
}
