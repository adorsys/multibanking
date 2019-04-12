package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import de.adorsys.multibanking.repository.BookingRuleRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BookingRuleRepositoryImpl implements BookingRuleRepositoryIf {

    private final BookingRuleRepositoryMongodb ruleRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<RuleEntity> findByUserId(String userId) {
        return ruleRepository.findByUserId(userId);
    }

    public Page<RuleEntity> findAllPageable(Pageable pageable) {
        return ruleRepository.findAll(pageable);
    }

    public List<RuleEntity> findAll() {
        return ruleRepository.findAll();
    }

    @Override
    public RuleEntity createOrUpdateRule(RuleEntity ruleEntity) {
        ruleEntity.updateSearchIndex();
        return ruleRepository.save(ruleEntity);
    }

    @Override
    public List<RuleEntity> search(String text) {
        Collection<String> terms = new HashSet(Arrays.asList(text.split(" ")));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        return mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)), RuleEntity.class);
    }

    @Override
    public Optional<RuleEntity> findById(String id) {
        return ruleRepository.findById(id);
    }

    @Override
    public Optional<RuleEntity> findByRuleId(String ruleId) {
        return ruleRepository.findByRuleId(ruleId);
    }

    @Override
    public void deleteRule(String id) {
        ruleRepository.deleteById(id);
    }

}
