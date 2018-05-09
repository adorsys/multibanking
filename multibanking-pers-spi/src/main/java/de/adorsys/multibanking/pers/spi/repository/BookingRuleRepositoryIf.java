package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 04.12.17
 */
public interface BookingRuleRepositoryIf {

    List<RuleEntity> findByUserId(String userId);

    Page<RuleEntity> findAllPageable(Pageable pageable);

    List<RuleEntity> findAll();

    RuleEntity createOrUpdateRule(RuleEntity ruleEntity);

    List<RuleEntity> search(String query);

    Optional<RuleEntity> getRuleById(String ruleId);

    void deleteRule(String id);
}
