package de.adorsys.mbs.service.example.config;

import de.adorsys.multibanking.service.analytics.AnalyticsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by peter on 13.04.18 at 13:22.
 */
@Configuration
public class AnalyticsServiceConfig {

    @Bean
    public AnalyticsService analyticsService() {
       return new AnalyticsService();
    }
}
