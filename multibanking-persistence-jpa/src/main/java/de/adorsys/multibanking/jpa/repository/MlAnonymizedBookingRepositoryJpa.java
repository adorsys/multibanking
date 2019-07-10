package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.MlAnonymizedBookingJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"jpa"})
public interface MlAnonymizedBookingRepositoryJpa extends JpaRepository<MlAnonymizedBookingJpaEntity, String> {

    List<MlAnonymizedBookingJpaEntity> findByUserId(String userId);

    long deleteByUserId(String userId);
}
