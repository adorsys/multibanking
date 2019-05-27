package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.StandingOrderRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class StandingOrderRepositoryImpl implements StandingOrderRepositoryIf {

    private final StandingOrderRepositoryMongodb standingOrderRepositoryMongodb;
    private final MongoEntityMapper entityMapper;

    @Override
    public List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return entityMapper.mapToStandingOrderEntities(standingOrderRepositoryMongodb.findByUserIdAndAccountId(userId
                , accountId));
    }

    @Override
    public void save(List<StandingOrderEntity> standingOrders) {
        standingOrderRepositoryMongodb.insert(entityMapper.mapToStandingOrderMongoEntities(standingOrders));
    }

    @Override
    public void save(StandingOrderEntity standingOrder) {
        standingOrderRepositoryMongodb.save(entityMapper.mapToStandingOrderMongoEntity(standingOrder));
    }

    @Override
    public void deleteByAccountId(String accountId) {
        standingOrderRepositoryMongodb.deleteByAccountId(accountId);
    }
}
