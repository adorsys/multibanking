package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.CustomRuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo"})
public interface CustomRuleRepositoryMongodb extends MongoRepository<CustomRuleEntity, String> {

    List<CustomRuleEntity> findByIncoming(boolean incoming);

    Optional<CustomRuleEntity> getRuleById(String ruleId);
}
