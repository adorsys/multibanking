package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.KeyStoreEntity;
import de.adorsys.multibanking.pers.spi.repository.KeyStoreRepositoryIf;
import de.adorsys.multibanking.repository.KeyStoreRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class KeyStoreRepositoryImpl implements KeyStoreRepositoryIf {

    @Autowired
    private KeyStoreRepositoryMongodb keystoreRepository;

	@Override
	public void save(KeyStoreEntity keyStoreEntity) {
		keystoreRepository.save(keyStoreEntity);
	}

	@Override
	public KeyStoreEntity findOne(String name) {
		return keystoreRepository.findOne(name);
	}
}
