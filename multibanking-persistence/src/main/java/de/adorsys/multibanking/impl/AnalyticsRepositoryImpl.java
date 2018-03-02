package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.repository.AnalyticsRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

    @Autowired
    private AnalyticsRepositoryMongodb analyticsRepository;
	
	@Override
	public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
		return analyticsRepository.findLastByUserIdAndAccountId(userId, bankAccountId);
	}

	@Override
	public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
		analyticsRepository.save(accountAnalyticsEntity);
	}

	@Override
	public void deleteByAccountId(String id) {
		analyticsRepository.deleteByAccountId(id);
	}

}
