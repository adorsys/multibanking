package de.adorsys.multibanking.config;

import de.adorsys.smartanalytics.config.EnableSmartanalytics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@EnableSmartanalytics
@Configuration
@Profile("smartanalytics-embedded")
public class SmartanalyticsEmbeddedConfig {
}
