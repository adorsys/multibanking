package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BankJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BankRepositoryJpa extends JpaRepository<BankJpaEntity, String> {

    Optional<BankJpaEntity> findByBankCode(String bankCode);

    @Query(value = "SELECT banking_url FROM bank_jpa_entity WHERE bank_code = ?1", nativeQuery = true)
    String findBankingUrl(String bankCode);

}
