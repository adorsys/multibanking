package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import de.adorsys.multibanking.repository.StandingOrderRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class StandingOrderRepositoryImpl implements StandingOrderRepositoryIf {

    private final StandingOrderRepositoryMongodb standingOrderRepositoryMongodb;

    @Override
    public List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return standingOrderRepositoryMongodb.findByUserIdAndAccountId(userId, accountId);
    }

    @Override
    public void save(List<StandingOrderEntity> standingOrders) {
        standingOrderRepositoryMongodb.insert(standingOrders);
    }

    @Override
    public void save(StandingOrderEntity standingOrder) {
        standingOrderRepositoryMongodb.save(standingOrder);
    }

    @Override
    public void deleteByAccountId(String accountId) {
        standingOrderRepositoryMongodb.deleteByAccountId(accountId);
    }
}
