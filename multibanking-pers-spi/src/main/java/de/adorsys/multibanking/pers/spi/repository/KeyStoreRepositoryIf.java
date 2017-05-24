package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.KeyStoreEntity;

/**
 * Created by alexg on 24.05.17.
 */
public interface KeyStoreRepositoryIf {

    void save(KeyStoreEntity keyStoreEntity);

    KeyStoreEntity findOne(String name);
}
