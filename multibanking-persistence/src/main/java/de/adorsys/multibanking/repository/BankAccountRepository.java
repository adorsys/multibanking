package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
public interface BankAccountRepository extends MongoRepository<BankAccountEntity, String> {

    List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId);

    Optional<BankAccountEntity> findByUserIdAndId(String userId, String id);
}
