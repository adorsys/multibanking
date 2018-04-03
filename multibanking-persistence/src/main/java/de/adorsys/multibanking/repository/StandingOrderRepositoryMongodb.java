package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface StandingOrderRepositoryMongodb extends MongoRepository<StandingOrderEntity, String> {

    List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByAccountId(String accountId);


}
