package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.domain.ContractEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 04.09.17
 */
public interface ContractRepositoryIf {

    List<ContractEntity> findByUserIdAndAccountId(String userId, String accountId);

    void save(List<ContractEntity> contractEntities);

    void deleteByAccountId(String accountId);
}
