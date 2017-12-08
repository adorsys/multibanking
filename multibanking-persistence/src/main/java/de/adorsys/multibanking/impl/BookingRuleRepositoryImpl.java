package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import de.adorsys.multibanking.repository.CustomRuleRepositoryMongodb;
import de.adorsys.multibanking.repository.RuleRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile({"mongo", "fongo"})
@Service
public class BookingRuleRepositoryImpl implements BookingRuleRepositoryIf {

    @Autowired
    private RuleRepositoryMongodb ruleRepository;

    @Autowired
    private CustomRuleRepositoryMongodb customRuleRepository;

    public List<RuleEntity> findByIncoming(boolean incoming) {
        return ruleRepository.findByIncoming(incoming);
    }

    public List<CustomRuleEntity> findByIncomingCustomRules(boolean incoming) {
        return customRuleRepository.findByIncoming(incoming);
    }

    public CustomRuleEntity createRule(CustomRuleEntity ruleEntity) {
        return customRuleRepository.save(ruleEntity);
    }


}
