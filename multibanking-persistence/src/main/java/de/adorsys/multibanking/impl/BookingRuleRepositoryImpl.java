package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import de.adorsys.multibanking.repository.CustomRuleRepositoryMongodb;
import de.adorsys.multibanking.repository.RuleRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class BookingRuleRepositoryImpl implements BookingRuleRepositoryIf {

    @Autowired
    private RuleRepositoryMongodb ruleRepository;

    @Autowired
    private CustomRuleRepositoryMongodb customRuleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<RuleEntity> findByIncoming(boolean incoming) {
        return ruleRepository.findByIncoming(incoming);
    }

    public List<CustomRuleEntity> findByUserIdAndIncomingCustomRules(String userId, boolean incoming) {
        return customRuleRepository.findByIncoming(incoming);
    }

    public Page<? extends RuleEntity> findAllPageable(Pageable pageable, boolean custom) {
        if (custom) {
            return customRuleRepository.findAll(pageable);
        } else {
            return ruleRepository.findAll(pageable);
        }
    }

    public List<? extends RuleEntity> findAll(boolean custom) {
        if (custom) {
            return customRuleRepository.findAll();
        } else {
            return ruleRepository.findAll();
        }
    }

    @Override
    public CustomRuleEntity createOrUpdateCustomRule(CustomRuleEntity ruleEntity) {
        ruleEntity.updateSearchIndex();
        return customRuleRepository.save(ruleEntity);
    }

    @Override
    public RuleEntity createOrUpdateRule(RuleEntity ruleEntity) {
        ruleEntity.updateSearchIndex();
        return ruleRepository.save(ruleEntity);
    }

    @Override
    public List<? extends RuleEntity> search(boolean customRules, String text) {
        Collection<String> terms = new HashSet(Arrays.asList(text.split(" ")));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        if (customRules) {
            return mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)), CustomRuleEntity.class);
        }
        return mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)), RuleEntity.class);
    }

    @Override
    public Optional<? extends RuleEntity> getRuleById(boolean customRule, String ruleId) {
        if (customRule) {
            return customRuleRepository.getRuleById(ruleId);
        }
        return ruleRepository.getRuleById(ruleId);
    }

    @Override
    public void deleteCustomRule(String id) {
        customRuleRepository.delete(id);
    }

    @Override
    public void deleteRule(String id) {
        ruleRepository.delete(id);
    }

    @Override
    public void replacesRules(List<? extends RuleEntity> rules, boolean custom) {
        if (custom) {
            mongoTemplate.remove(new Query(), CustomRuleEntity.class);
            customRuleRepository.save((List<CustomRuleEntity>) rules);
        } else {
            mongoTemplate.remove(new Query(), RuleEntity.class);
            ruleRepository.save(rules);
        }
    }

}
