package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.mongo.entity.AccountAnalyticsMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.AnalyticsRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

    private final AnalyticsRepositoryMongodb analyticsRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
        return analyticsRepository.findLastByUserIdAndAccountId(userId, bankAccountId)
                .map(entityMapper::mapToAccountAnalyticsEntity);
    }

    @Override
    public Optional<LocalDateTime> findLastAnalyticsDateByUserIdAndAccountId(String userId, String bankAccountId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("accountId").is(bankAccountId));
        query.fields().include("analyticsDate");

        return Optional.ofNullable(mongoTemplate.findOne(query, AccountAnalyticsMongoEntity.class))
                .map(AccountAnalyticsMongoEntity::getAnalyticsDate);
    }

    @Override
    public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
        analyticsRepository.save(entityMapper.mapToAccountAnalyticsMongoEntity(accountAnalyticsEntity));
    }

    @Override
    public void deleteByAccountId(String id) {
        analyticsRepository.deleteByAccountId(id);
    }

}
