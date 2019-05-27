package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.AccountAnalyticsJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface AnalyticsRepositoryJpa extends JpaRepository<AccountAnalyticsJpaEntity, String> {

    Optional<AccountAnalyticsJpaEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId);

    void deleteByAccountId(String id);

    @Query(value = "SELECT max(analyticsDate) FROM #{#entityName} a WHERE a.userId=:userId and a" +
            ".accountId=:accountId")
    Optional<LocalDateTime> findLastAnalyticsDateByUserIdAndAccountId(@Param("userId") String userId, @Param(
            "accountId") String accountId);

}
