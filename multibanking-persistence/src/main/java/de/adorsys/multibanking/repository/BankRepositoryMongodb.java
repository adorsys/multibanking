package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface BankRepositoryMongodb extends MongoRepository<BankEntity, String> {

    Optional<BankEntity> findByBankCode(String bankCode);
}
