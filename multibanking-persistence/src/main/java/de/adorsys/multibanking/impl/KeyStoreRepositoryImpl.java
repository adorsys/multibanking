package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.KeyStoreEntity;
import de.adorsys.multibanking.pers.spi.repository.KeystoreRepositoryIf;
import de.adorsys.multibanking.repository.KeystoreRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"mongo", "fongo"})
@Service
public class KeystoreRepositoryImpl implements KeystoreRepositoryIf {

    @Autowired
    private KeystoreRepositoryMongodb keystoreRepositoryMongodb;

    @Override
    public KeyStoreEntity findByName(String name) {
        return keystoreRepositoryMongodb.findByName(name);
    }

    @Override
    public long countByName(String name) {
        return keystoreRepositoryMongodb.countByName(name);
    }

    @Override
    public void save(KeyStoreEntity keyStoreEntity) {
        keystoreRepositoryMongodb.save(keyStoreEntity);
    }
}
