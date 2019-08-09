package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BankAccessJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BankAccessRepositoryJpa extends JpaRepository<BankAccessJpaEntity, Long> {

    Optional<BankAccessJpaEntity> findByUserIdAndId(String userId, Long id);

    List<BankAccessJpaEntity> findByUserId(String userId);

    void deleteByUserIdAndId(String userId, Long id);

    @Query(value = "SELECT bankCode FROM #{#entityName} a WHERE a.id=:id")
    String getBankCode(@Param("id") Long id);

    List<BankAccessJpaEntity> findByUserIdAndConsentId(String userId, String consentId);
}
