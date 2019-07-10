package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.RawSepaTransactionJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface RawSepaTransactionRepositoryJpa extends JpaRepository<RawSepaTransactionJpaEntity, String> {

    Optional<RawSepaTransactionJpaEntity> findByUserIdAndId(String userId, String id);

}
