package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
public interface BookingRuleRepositoryMongodb extends MongoRepository<RuleEntity, String> {

    Page<RuleEntity> findAll(Pageable pageable);

    List<RuleEntity> findByIncoming(boolean incoming);

    List<RuleEntity> findByUserId(String userId);

    Optional<RuleEntity> getRuleById(String ruleId);
}
