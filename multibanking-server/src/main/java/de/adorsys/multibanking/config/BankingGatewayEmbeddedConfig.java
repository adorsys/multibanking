package de.adorsys.multibanking.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(
    basePackages = "de.adorsys.banking",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de\\.adorsys\\.banking\\.config\\..*")
    }
)
@Profile("bankinggateway-b2c-embedded")
public class BankingGatewayEmbeddedConfig {
}
