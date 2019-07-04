package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.ContractRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class ContractRepositoryImpl implements ContractRepositoryIf {

    private final ContractRepositoryJpa repository;
    private final JpaEntityMapper entityMapper;

    @Override
    public List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return entityMapper.mapToContractEntities(repository.findByUserIdAndAccountId(userId,
                accountId));
    }

    @Override
    public void save(List<ContractEntity> contractEntities) {
        repository.saveAll(entityMapper.mapToContractJpaEntities(contractEntities));
    }

    @Override
    public void deleteByAccountId(String accountId) {
        repository.deleteByAccountId(accountId);
    }
}
