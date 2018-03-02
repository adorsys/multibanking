package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import de.adorsys.multibanking.repository.StandingOrderRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class StandingOrderRepositoryImpl implements StandingOrderRepositoryIf {

    @Autowired
    private StandingOrderRepositoryMongodb standingOrderRepositoryMongodb;


    @Override
    public List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return standingOrderRepositoryMongodb.findByUserIdAndAccountId(userId, accountId);
    }

    @Override
    public void save(List<StandingOrderEntity> standingOrders) {
        standingOrderRepositoryMongodb.insert(standingOrders);
    }

    @Override
    public void deleteByAccountId(String accountId) {
        standingOrderRepositoryMongodb.deleteByAccountId(accountId);
    }
}
