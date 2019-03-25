package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.StandingOrderEntity;

import java.util.List;

/**
 * @author alexg on 04.09.17
 */
public interface StandingOrderRepositoryIf {

    List<StandingOrderEntity> findByUserIdAndAccountId(String userId, String accountId);

    void save(List<StandingOrderEntity> standingOrders);

    void save(StandingOrderEntity standingOrder);

    void deleteByAccountId(String accountId);
}
