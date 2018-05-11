package de.adorsys.mbs.service.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.service.interceptor.TokenBasedMockBanking;
import de.adorsys.onlinebanking.mock.MockBanking;

@Configuration
@Profile("!IntegrationTest")
public class MockBankingConfig {
	@Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MockBanking mockBanking(UserContext userContext){
		return new TokenBasedMockBanking(userContext);
	}
}
