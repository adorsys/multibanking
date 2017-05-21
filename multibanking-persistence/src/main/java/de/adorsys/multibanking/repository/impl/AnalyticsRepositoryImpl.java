package de.adorsys.multibanking.repository.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIF;
import de.adorsys.multibanking.repository.AnalyticsRepository;

@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIF {

    @Autowired
    private AnalyticsRepository analyticsRepository;
	
	@Override
	public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
		return analyticsRepository.findLastByUserIdAndAccountId(userId, bankAccountId);
	}

	@Override
	public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
		analyticsRepository.save(accountAnalyticsEntity);
	}

}
