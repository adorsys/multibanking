package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankInfoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo"})
public interface BankInfoRepositoryMongodb extends MongoRepository<BankInfoEntity, String> {

    Optional<BankInfoEntity> findByBankCode(String blz);
}
