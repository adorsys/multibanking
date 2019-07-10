package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.AnonymizedBookingJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"jpa"})
public interface AnonymizedBookingRepositoryJpa extends JpaRepository<AnonymizedBookingJpaEntity, String> {

}
