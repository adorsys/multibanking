package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 04.12.17
 */
public interface BookingRuleRepositoryIf {

    List<RuleEntity> findByIncoming(boolean incoming);

    List<CustomRuleEntity> findByUserId(String userId);

    Page<? extends RuleEntity> findAllPageable(Pageable pageable, boolean custom);

    List<? extends RuleEntity> findAll(boolean custom);

    RuleEntity createOrUpdateCustomRule(CustomRuleEntity ruleEntity);

    RuleEntity createOrUpdateRule(RuleEntity ruleEntity);

    List<? extends RuleEntity> search(boolean customRules, String query);

    Optional<? extends RuleEntity> getRuleById(boolean customRule, String ruleId);

    void deleteCustomRule(String id);

    void deleteRule(String id);

    void replacesRules(List<? extends RuleEntity> rules, boolean custom);
}
