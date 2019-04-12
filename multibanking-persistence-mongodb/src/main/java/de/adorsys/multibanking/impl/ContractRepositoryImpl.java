package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import de.adorsys.multibanking.repository.ContractRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class ContractRepositoryImpl implements ContractRepositoryIf {

    private final ContractRepositoryMongodb contractRepositoryMongodb;

    @Override
    public List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return contractRepositoryMongodb.findByUserIdAndAccountId(userId, accountId);
    }

    @Override
    public void save(List<ContractEntity> contractEntities) {
        contractRepositoryMongodb.insert(contractEntities);
    }

    @Override
    public void deleteByAccountId(String accountId) {
        contractRepositoryMongodb.deleteByAccountId(accountId);
    }
}
