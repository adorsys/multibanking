package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.ContractEntity;
import de.adorsys.multibanking.pers.spi.repository.ContractRepositoryIf;
import de.adorsys.multibanking.jpa.repository.ContractRepositoryJpa;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class ContractRepositoryImpl implements ContractRepositoryIf {

    private final ContractRepositoryJpa contractRepositoryMongodb;

    @Override
    public List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return null;
    }

    @Override
    public void save(List<ContractEntity> contractEntities) {

    }

    @Override
    public void deleteByAccountId(String accountId) {

    }
}
