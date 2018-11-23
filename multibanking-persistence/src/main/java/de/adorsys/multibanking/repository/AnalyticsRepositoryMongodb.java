package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo"})
public interface AnalyticsRepositoryMongodb extends MongoRepository<AccountAnalyticsEntity, String> {

    Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId);

    void deleteByAccountId(String id);

}
