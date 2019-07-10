package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.RuleJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BookingRuleRepositoryJpa extends JpaRepository<RuleJpaEntity, String> {

    List<RuleJpaEntity> findByUserId(String userId);

    Optional<RuleJpaEntity> findByRuleId(String ruleId);

}
