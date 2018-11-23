package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import de.adorsys.multibanking.repository.AnalyticsRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

    @Autowired
    private AnalyticsRepositoryMongodb analyticsRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
		return analyticsRepository.findLastByUserIdAndAccountId(userId, bankAccountId);
	}

	@Override
	public Optional<LocalDateTime> findLastAnalyticsDateByUserIdAndAccountId(String userId, String bankAccountId) {
		Query query = Query.query(Criteria.where("userId").is(userId).and("accountId").is(bankAccountId));
		query.fields().include("bankCode");

        AccountAnalyticsEntity found = mongoTemplate.findOne(query, AccountAnalyticsEntity.class);
		return found != null ? Optional.of(found.getAnalyticsDate()) : Optional.empty();
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
