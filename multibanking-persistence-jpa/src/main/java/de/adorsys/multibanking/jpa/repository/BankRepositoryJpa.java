package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BankJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BankRepositoryJpa extends JpaRepository<BankJpaEntity, String> {

    Optional<BankJpaEntity> findByBankCode(String bankCode);

}
