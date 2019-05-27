package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.RuleJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"jpa"})
public interface RuleRepositoryJpa extends JpaRepository<RuleJpaEntity, String> {

    Page<RuleJpaEntity> findAll(Pageable pageable);

}
