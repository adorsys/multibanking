package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.ContractRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class ContractRepositoryImpl implements ContractRepositoryIf {

    private final ContractRepositoryMongodb contractRepositoryMongodb;
    private final MongoEntityMapper entityMapper;

    @Override
    public List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return entityMapper.mapToContractEntities(contractRepositoryMongodb.findByUserIdAndAccountId(userId,
                accountId));
    }

    @Override
    public void save(List<ContractEntity> contractEntities) {
        contractRepositoryMongodb.insert(entityMapper.mapToContractMongoEntities(contractEntities));
    }

    @Override
    public void deleteByAccountId(String accountId) {
        contractRepositoryMongodb.deleteByAccountId(accountId);
    }
}
