package de.adorsys.multibanking.config.authserver;

import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.config.core.STSInMemoryConfig;
import de.adorsys.multibanking.service.base.ExceptionHandlingDocumentSafeService;
import de.adorsys.multibanking.utils.Ids;
import de.adorsys.sts.persistence.FsUserDataRepository;
import de.adorsys.sts.resourceserver.persistence.InMemoryResourceServerRepository;
import de.adorsys.sts.resourceserver.persistence.ResourceServerRepository;
import de.adorsys.sts.resourceserver.service.UserDataRepository;
import de.adorsys.sts.token.passwordgrant.EnablePasswordGrant;

@Configuration
@EnablePasswordGrant
@Import({ STSInMemoryConfig.class })
@EnableConfigurationProperties
@Profile("IntegrationTest")
public class PasswordGrantAuthServerConfig {

	@Bean
	ResourceServerRepository resourceServerRepository() {
		return new InMemoryResourceServerRepository();
	}

	@Bean
	UserDataRepository userDataRepository(ObjectMapper objectMapper) {
		// Warning you can not use the client applications docusafe here.
		FileSystemExtendedStorageConnection storageConnection = new FileSystemExtendedStorageConnection("target/authServer/"+Ids.uuid());
		DocumentSafeServiceImpl documentSafeServiceImpl = new DocumentSafeServiceImpl(storageConnection);
		ExceptionHandlingDocumentSafeService documentSafeService = new ExceptionHandlingDocumentSafeService(documentSafeServiceImpl);
		return new FsUserDataRepository(documentSafeService, objectMapper);
	}
}
