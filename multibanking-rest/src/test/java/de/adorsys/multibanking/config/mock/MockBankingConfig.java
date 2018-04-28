package de.adorsys.multibanking.config.mock;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.onlinebanking.mock.MockBanking;

@Configuration
@Profile("IntegrationTest")
public class MockBankingConfig {
	SimpleMockBanking simpleMockBanking;
	
	@PostConstruct
	public void postConstruct(){
		InputStream bookingsStream = SimpleMockBanking.class.getClassLoader().getResourceAsStream("/test_data.xls");
		simpleMockBanking = new SimpleMockBanking(null, null, bookingsStream);		
	}
	
	@Bean
	public MockBanking mockBanking(UserContext userContext){
		return simpleMockBanking;
	}
}
