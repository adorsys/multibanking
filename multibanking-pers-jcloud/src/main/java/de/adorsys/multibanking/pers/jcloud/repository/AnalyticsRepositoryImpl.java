package de.adorsys.multibanking.pers.jcloud.repository;

import java.util.Optional;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;

public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

	@Override
	public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
		// TODO Auto-generated method stub

	}

}
