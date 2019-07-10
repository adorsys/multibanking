package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.StandingOrderEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
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

    private final StandingOrderRepositoryJpa standingOrderRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return entityMapper.mapToStandingOrderEntities(standingOrderRepository.findByUserIdAndAccountId(userId
                , accountId));
    }

    @Override
    public void save(List<StandingOrderEntity> standingOrders) {
        standingOrderRepository.saveAll(entityMapper.mapToStandingOrderJpaEntities(standingOrders));
    }

    @Override
    public void save(StandingOrderEntity standingOrder) {
        standingOrderRepository.save(entityMapper.mapToStandingOrderJpaEntity(standingOrder));
    }

    @Override
    public void deleteByAccountId(String accountId) {
        standingOrderRepository.deleteByAccountId(accountId);
    }
}
