package de.adorsys.multibanking.jpa.conf;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"de.adorsys.multibanking.jpa.entity"})
@EnableJpaRepositories(basePackages = "de.adorsys.multibanking.jpa.repository")
public class JpaConfig {
}
