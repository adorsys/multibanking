package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.jpa.entity.BankAccountJpaEntity;
import de.adorsys.multibanking.jpa.entity.ConsentJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Profile({"jpa"})
public interface ConsentRepositoryJpa extends JpaRepository<ConsentJpaEntity, String> {

    Optional<ConsentJpaEntity> findByRedirectId(String id);
}
