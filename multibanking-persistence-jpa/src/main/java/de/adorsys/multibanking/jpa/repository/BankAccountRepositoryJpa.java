package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BankAccountJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BankAccountRepositoryJpa extends JpaRepository<BankAccountJpaEntity, Long> {

    List<BankAccountJpaEntity> findByUserId(String userId);

    List<BankAccountJpaEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId);

    Optional<BankAccountJpaEntity> findByUserIdAndId(String userId, Long id);

    List<BankAccountJpaEntity> deleteByBankAccessId(String accessId);
}
