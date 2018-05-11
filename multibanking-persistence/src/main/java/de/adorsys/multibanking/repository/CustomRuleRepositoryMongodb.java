package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
public interface CustomRuleRepositoryMongodb extends MongoRepository<CustomRuleEntity, String> {

    Page<CustomRuleEntity> findAll(Pageable pageable);

    List<CustomRuleEntity> findByIncoming(boolean incoming);

    List<CustomRuleEntity> findByUserId(String userId);

    Optional<CustomRuleEntity> getRuleById(String ruleId);
}
