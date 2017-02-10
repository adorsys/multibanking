package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
public interface BankAccessRepository extends MongoRepository<BankAccess, String> {

    Optional<BankAccess> findById(String id);

    Optional<List<BankAccess>> findByUserId(String userId);
}
