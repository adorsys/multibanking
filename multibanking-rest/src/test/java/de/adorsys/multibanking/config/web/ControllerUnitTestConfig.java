package de.adorsys.multibanking.config.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.service.old.TestConstants;

/**
 * This is the configuration for unit testing web controller. We mock inject user so no need for including
 * security filters.
 *
 * @author fpo
 *
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, MongoAutoConfiguration.class})
public class ControllerUnitTestConfig {

    @Bean
    @Primary
    public SystemContext systemIDAuth(){
    	return TestConstants.getSystemUserIDAuth();
    }
}
