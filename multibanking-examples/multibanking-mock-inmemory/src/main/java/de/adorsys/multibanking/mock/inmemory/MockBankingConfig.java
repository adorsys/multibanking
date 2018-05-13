package de.adorsys.multibanking.mock.inmemory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.adorsys.onlinebanking.mock.MockBanking;

@Configuration
@Profile("IntegrationTest")
public class MockBankingConfig {

	@Bean
	public MockBanking mockBanking(){
		return new SimpleMockBanking();
	}
}
