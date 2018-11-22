package de.adorsys.multibanking.config.mock;

import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.mock.inmemory.SimpleMockBanking;
import de.adorsys.onlinebanking.mock.MockBanking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Profile("IntegrationTest")
public class MockBankingConfig {
    SimpleMockBanking simpleMockBanking;

    @PostConstruct
    public void postConstruct() {
        // TODO: fix this
//		InputStream bookingsStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("/test_data.xls");
        simpleMockBanking = new SimpleMockBanking(null, null);
    }

    @Bean
    public MockBanking mockBanking(UserContext userContext) {
        return simpleMockBanking;
    }
}
