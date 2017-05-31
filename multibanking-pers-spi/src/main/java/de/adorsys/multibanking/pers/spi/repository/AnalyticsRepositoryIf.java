package de.adorsys.multibanking.pers.spi.repository;

import java.util.Optional;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface AnalyticsRepositoryIf {

    Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId);

	void save(AccountAnalyticsEntity accountAnalyticsEntity);

}
