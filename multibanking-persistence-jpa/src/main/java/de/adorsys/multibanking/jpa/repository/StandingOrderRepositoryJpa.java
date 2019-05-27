package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.StandingOrderJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"jpa"})
public interface StandingOrderRepositoryJpa extends JpaRepository<StandingOrderJpaEntity, String> {

    List<StandingOrderJpaEntity> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByAccountId(String accountId);

}
