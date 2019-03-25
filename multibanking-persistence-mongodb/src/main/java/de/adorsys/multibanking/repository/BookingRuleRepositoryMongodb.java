package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface BookingRuleRepositoryMongodb extends MongoRepository<RuleEntity, String> {

    Page<RuleEntity> findAll(Pageable pageable);

    List<RuleEntity> findByUserId(String userId);

    Optional<RuleEntity> findByRuleId(String ruleId);

}
