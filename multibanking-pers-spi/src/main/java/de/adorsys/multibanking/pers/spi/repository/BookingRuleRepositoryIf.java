package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.domain.RuleEntity;

import java.util.List;

/**
 * @author alexg on 04.12.17
 */
public interface BookingRuleRepositoryIf {

    List<RuleEntity> findByIncoming(boolean incoming);

    List<CustomRuleEntity> findByIncomingCustomRules(boolean incoming);

    RuleEntity createRule(CustomRuleEntity ruleEntity);
}
