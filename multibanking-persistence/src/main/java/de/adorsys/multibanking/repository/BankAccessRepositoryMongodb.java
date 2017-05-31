package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo"})
public interface BankAccessRepositoryMongodb extends MongoRepository<BankAccessEntity, String> {

    Optional<BankAccessEntity> findByUserIdAndId(String userId, String id);

    List<BankAccessEntity> findByUserId(String userId);
}
