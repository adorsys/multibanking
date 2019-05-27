package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.mongo.entity.RuleMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BookingRuleRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
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
    private final MongoEntityMapper entityMapper;

    @Override
    public List<RuleEntity> findByUserId(String userId) {
        return entityMapper.mapToRuleEntities(ruleRepository.findByUserId(userId));
    }

    public Page<RuleEntity> findAllPageable(Pageable pageable) {
        Page<RuleMongoEntity> pageRules = ruleRepository.findAll(pageable);
        return pageRules.map(entityMapper::mapToRuleEntity);

    }

    public List<RuleEntity> findAll() {
        return entityMapper.mapToRuleEntities(ruleRepository.findAll());
    }

    @Override
    public void createOrUpdateRule(RuleEntity ruleEntity) {
        ruleEntity.updateSearchIndex();
        ruleRepository.save(entityMapper.mapToRuleMongoEntity(ruleEntity));
    }

    @Override
    public List<RuleEntity> search(String text) {
        Collection<String> terms = new HashSet<>(Arrays.asList(text.split(" ")));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        return entityMapper.mapToRuleEntities(mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)),
                RuleMongoEntity.class));
    }

    @Override
    public Optional<RuleEntity> findById(String id) {
        return ruleRepository.findById(id)
                .map(entityMapper::mapToRuleEntity);
    }

    @Override
    public Optional<RuleEntity> findByRuleId(String ruleId) {
        return ruleRepository.findByRuleId(ruleId)
                .map(entityMapper::mapToRuleEntity);
    }

    @Override
    public void deleteRule(String id) {
        ruleRepository.deleteById(id);
    }

}
