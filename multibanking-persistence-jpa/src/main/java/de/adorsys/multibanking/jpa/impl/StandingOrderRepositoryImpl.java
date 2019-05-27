package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.jpa.repository.StandingOrderRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.StandingOrderRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class StandingOrderRepositoryImpl implements StandingOrderRepositoryIf {

    private final StandingOrderRepositoryJpa standingOrderRepositoryMongodb;

    @Override
    public List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return null;
    }

    @Override
    public void save(List<StandingOrderEntity> standingOrders) {

    }

    @Override
    public void save(StandingOrderEntity standingOrder) {

    }

    @Override
    public void deleteByAccountId(String accountId) {

    }
}
