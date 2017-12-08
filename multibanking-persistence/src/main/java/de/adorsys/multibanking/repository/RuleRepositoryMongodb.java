package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.RuleEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo"})
public interface RuleRepositoryMongodb extends MongoRepository<RuleEntity, String> {

    List<RuleEntity> findByIncoming(boolean incoming);

}
