package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.BulkPaymentJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"jpa"})
public interface BulkPaymentRepositoryJpa extends JpaRepository<BulkPaymentJpaEntity, String> {

}
