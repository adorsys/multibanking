package de.adorsys.multibanking.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "de.adorsys.multibanking"
})
public class MultibankingConfiguration {
}
