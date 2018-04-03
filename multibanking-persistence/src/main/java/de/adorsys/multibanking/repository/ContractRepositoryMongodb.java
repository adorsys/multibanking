package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.ContractEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by alexg on 07.02.17.
 */
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface ContractRepositoryMongodb extends MongoRepository<ContractEntity, String> {

    List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByAccountId(String accountId);


}
