package de.adorsys.multibanking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"jpa"})
@Configuration
//@EnableJpaRepositories(basePackages = "de.adorsys.multibanking.jpa.repository")
public class JpaConfig {
}
