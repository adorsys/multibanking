package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.PaymentJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface PaymentRepositoryJpa extends JpaRepository<PaymentJpaEntity, String> {

    Optional<PaymentJpaEntity> findByUserIdAndId(String userId, String id);

}
