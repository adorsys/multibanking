package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BookingsIndexJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BookingsIndexRepositoryJpa extends JpaRepository<BookingsIndexJpaEntity, String> {

    Optional<BookingsIndexJpaEntity> findByUserIdAndAccountId(String userId, String accountId);
}
