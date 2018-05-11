package de.adorsys.multibanking.config.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.adorsys.lockpersistence.client.LockClient;
import de.adorsys.lockpersistence.client.NoopLockClient;
import de.adorsys.sts.keymanagement.persistence.InMemoryKeyStoreRepository;
import de.adorsys.sts.keymanagement.persistence.KeyStoreRepository;

@Configuration
@Profile({"InMemory"})
public class STSInMemoryConfig {
	/**
	 * Inject in memory key store repository for tests
	 */
	private KeyStoreRepository keyStoreRepository = new InMemoryKeyStoreRepository();
	@Bean
	public KeyStoreRepository getKeyStoreRepository(){
		return keyStoreRepository;
	}
	
	/**
	 * Inject Noop lock client for test
	 */
	private LockClient lockClient = new NoopLockClient();
	@Bean
	public LockClient getLockClient(){
		return lockClient;
	}

}
