package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.AccountAnalyticsEntity;
import de.adorsys.multibanking.jpa.conf.JpaConfig;
import de.adorsys.multibanking.jpa.conf.MapperConfig;
import de.adorsys.multibanking.jpa.impl.AnalyticsRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("jpa")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JpaConfig.class, MapperConfig.class, AnalyticsRepositoryImpl.class})
@RunWith(SpringRunner.class)
public class AnalyticsRepositoryJpaTest {

    @Autowired
    private AnalyticsRepositoryImpl analyticsRepository;

    @Test
    public void test() {
        Optional<LocalDateTime> lastAnalyticsDate =
                analyticsRepository.findLastAnalyticsDateByUserIdAndAccountId(UUID.randomUUID().toString(),
                        UUID.randomUUID().toString());

        assertThat(lastAnalyticsDate).isEmpty();

        AccountAnalyticsEntity accountAnalyticsEntity = saveAnalytics();

        lastAnalyticsDate =
                analyticsRepository.findLastAnalyticsDateByUserIdAndAccountId(accountAnalyticsEntity.getUserId(),
                        accountAnalyticsEntity.getAccountId());

        assertThat(lastAnalyticsDate).isNotEmpty();

        Optional<AccountAnalyticsEntity> lastByUserIdAndAccountId =
                analyticsRepository.findLastByUserIdAndAccountId(accountAnalyticsEntity.getUserId(),
                        accountAnalyticsEntity.getAccountId());

        assertThat(lastByUserIdAndAccountId).isNotEmpty();

        analyticsRepository.deleteByAccountId(accountAnalyticsEntity.getAccountId());

        lastAnalyticsDate =
                analyticsRepository.findLastAnalyticsDateByUserIdAndAccountId(accountAnalyticsEntity.getUserId(),
                        accountAnalyticsEntity.getAccountId());

        assertThat(lastAnalyticsDate).isEmpty();
    }

    private AccountAnalyticsEntity saveAnalytics() {
        AccountAnalyticsEntity accountAnalyticsEntity = new AccountAnalyticsEntity();
        accountAnalyticsEntity.setAccountId(UUID.randomUUID().toString());
        accountAnalyticsEntity.setUserId(UUID.randomUUID().toString());

        analyticsRepository.save(accountAnalyticsEntity);

        return accountAnalyticsEntity;
    }
}
