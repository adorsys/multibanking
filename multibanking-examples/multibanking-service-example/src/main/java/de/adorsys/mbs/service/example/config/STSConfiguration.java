package de.adorsys.mbs.service.example.config;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.lockpersistence.client.LockClient;
import de.adorsys.lockpersistence.client.NoopLockClient;
import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.sts.decryption.EnableDecryption;
import de.adorsys.sts.keymanagement.persistence.KeyStoreRepository;
import de.adorsys.sts.keymanagement.service.KeyManagementProperties;
import de.adorsys.sts.keyrotation.EnableKeyRotation;
import de.adorsys.sts.persistence.FsKeyStoreRepository;
import de.adorsys.sts.persistence.KeyEntryMapper;
import de.adorsys.sts.pop.EnablePOP;
import de.adorsys.sts.token.authentication.EnableTokenAuthentication;

@Configuration
@EnableTokenAuthentication
@EnablePOP
@EnableDecryption
@EnableKeyRotation
public class STSConfiguration {

	@Value("${docusafe.system.user.name}")
	String docusafeSystemUserName;
	@Value("${docusafe.system.user.password}")
	String docusafeSystemUserPassword;

	@Bean
	KeyStoreRepository keyStoreRepository(ObjectMapper objectMapper, DocumentSafeService documentSafeService,
			KeyManagementProperties keyManagementProperties, SystemContext systemContext) {
		return new FsKeyStoreRepository(systemContext.getUser().getAuth(), documentSafeService, keyManagementProperties,
				new KeyEntryMapper(objectMapper));
	}

	@Bean
	public LockClient getLockClient() {
		return new NoopLockClient();
	}
}
