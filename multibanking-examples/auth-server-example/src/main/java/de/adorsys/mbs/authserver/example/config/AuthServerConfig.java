package de.adorsys.mbs.authserver.example.config;

import javax.annotation.PostConstruct;

import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.adorsys.encobject.filesystem.FileSystemExtendedStorageConnection;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.lockpersistence.client.LockClient;
import de.adorsys.lockpersistence.client.NoopLockClient;
import de.adorsys.sts.keymanagement.persistence.KeyStoreRepository;
import de.adorsys.sts.keymanagement.service.KeyManagementProperties;
import de.adorsys.sts.persistence.FsKeyStoreRepository;
import de.adorsys.sts.persistence.FsResourceServerRepository;
import de.adorsys.sts.persistence.FsUserDataRepository;
import de.adorsys.sts.persistence.KeyEntryMapper;
import de.adorsys.sts.resourceserver.persistence.ResourceServerRepository;
import de.adorsys.sts.resourceserver.service.UserDataRepository;
import de.adorsys.sts.token.passwordgrant.EnablePasswordGrant;

@Configuration
@EnablePasswordGrant
@EnableConfigurationProperties
public class AuthServerConfig {

    @Value("${docusafe.system.user.name}")
    String docusafeSystemUserName;
    @Value("${docusafe.system.user.password}")
    String docusafeSystemUserPassword;
    
    UserIDAuth systemId;
    
    @PostConstruct
    void postConstruct(){
		systemId = new UserIDAuth(new UserID(docusafeSystemUserName), new ReadKeyPassword(docusafeSystemUserPassword));
    }

	@Bean
	DocumentSafeService documentSafeService() {
		FileSystemExtendedStorageConnection storageConnection = new FileSystemExtendedStorageConnection("target/authServer/"+RandomStringUtils.randomAlphanumeric(10).toUpperCase());
		DocumentSafeServiceImpl documentSafeService= new DocumentSafeServiceImpl(storageConnection);
		// Create system user.
		if(!documentSafeService.userExists(systemId.getUserID())){
			documentSafeService.createUser(systemId);
		}
		return documentSafeService;
	}
    
	@Bean
	ResourceServerRepository resourceServerRepository(ObjectMapper objectMapper, DocumentSafeService documentSafeService) {
		return new FsResourceServerRepository(systemId, documentSafeService, objectMapper);
	}

	@Bean
	UserDataRepository userDataRepository(ObjectMapper objectMapper, DocumentSafeService documentSafeService) {
		return new FsUserDataRepository(documentSafeService, objectMapper);
	}

	@Bean
	KeyStoreRepository keyStoreRepository(ObjectMapper objectMapper, DocumentSafeService documentSafeService, KeyManagementProperties keyManagementProperties) {
		return new FsKeyStoreRepository(systemId, documentSafeService, keyManagementProperties, new KeyEntryMapper(objectMapper));
	}
	
	private LockClient lockClient = new NoopLockClient();
	@Bean
	public LockClient getLockClient(){
		return lockClient;
	}
}
