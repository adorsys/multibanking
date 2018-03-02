package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface RuleRepositoryMongodb extends MongoRepository<RuleEntity, String> {

    Page<RuleEntity> findAll(Pageable pageable);

    List<RuleEntity> findByIncoming(boolean incoming);

    Optional<RuleEntity> getRuleById(String ruleId);
}
