package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.RuleEntity;
import de.adorsys.multibanking.jpa.entity.RuleJpaEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BookingRuleRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BookingRuleRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BookingRuleRepositoryImpl implements BookingRuleRepositoryIf {

    private final BookingRuleRepositoryJpa ruleRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public List<RuleEntity> findByUserId(String userId) {
        return entityMapper.mapToRuleEntities(ruleRepository.findByUserId(userId));
    }

    public Page<RuleEntity> findAllPageable(Pageable pageable) {
        Page<RuleJpaEntity> pageRules = ruleRepository.findAll(pageable);
        return pageRules.map(entityMapper::mapToRuleEntity);

    }

    public List<RuleEntity> findAll() {
        return entityMapper.mapToRuleEntities(ruleRepository.findAll());
    }

    @Override
    public void createOrUpdateRule(RuleEntity ruleEntity) {
        ruleEntity.updateSearchIndex();
        ruleRepository.save(entityMapper.mapToRuleJpaEntity(ruleEntity));
    }

    @Override
    public List<RuleEntity> search(String text) {
        return null;
//        Collection<String> terms = new HashSet<>(Arrays.asList(text.split(" ")));
//
//        Criteria[] criterias = terms
//                .stream()
//                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
//                .toArray(Criteria[]::new);
//
//        return entityMapper.mapToRuleEntities(mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)),
//                RuleMongoEntity.class));
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
