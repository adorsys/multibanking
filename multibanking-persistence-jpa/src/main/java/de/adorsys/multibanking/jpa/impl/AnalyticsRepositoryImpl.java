package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.jpa.entity.AccountAnalyticsJpaEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.AnalyticsRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.AnalyticsRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class AnalyticsRepositoryImpl implements AnalyticsRepositoryIf {

    private final AnalyticsRepositoryJpa analyticsRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public Optional<AccountAnalyticsEntity> findLastByUserIdAndAccountId(String userId, String bankAccountId) {
        return analyticsRepository.findLastByUserIdAndAccountId(userId, bankAccountId)
                .map(entityMapper::mapToAccountAnalyticsEntity);
    }

    @Override
    public Optional<LocalDateTime> findLastAnalyticsDateByUserIdAndAccountId(String userId, String bankAccountId) {
        return analyticsRepository.findLastAnalyticsDateByUserIdAndAccountId(userId, bankAccountId);
    }

    @Override
    public void save(AccountAnalyticsEntity accountAnalyticsEntity) {
        AccountAnalyticsJpaEntity save =
                analyticsRepository.save(entityMapper.mapToAccountAnalyticsJpaEntity(accountAnalyticsEntity));
        accountAnalyticsEntity.setId(save.getId().toString());
    }

    @Override
    public void deleteByAccountId(String id) {
        analyticsRepository.deleteByAccountId(id);
    }
}
