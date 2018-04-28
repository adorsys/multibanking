package de.adorsys.mbs.service.example.config;

import de.adorsys.multibanking.analytics.DomainAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 13.04.18 at 13:10.
 */
@Configuration
public class DomainAnalyticsServiceConfig {

    @Bean
    public DomainAnalyticsService domainAnalyticsService() {
        return new DomainAnalyticsServiceImpl();
    }

    public static class DomainAnalyticsServiceImpl implements DomainAnalyticsService {
        private final static Logger LOGGER = LoggerFactory.getLogger(DomainAnalyticsServiceImpl.class);

        @Override
        public void startAccountAnalytics(String bankAccessId, String bankAccountId) {
            LOGGER.info("DomainAnalyticsService: " + bankAccessId + " " + bankAccountId);
        }
    }
}
