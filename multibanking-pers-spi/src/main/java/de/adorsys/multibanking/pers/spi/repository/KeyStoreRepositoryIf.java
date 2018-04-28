package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.KeyStoreEntity;

/**
 * @author alexg on 04.09.17
 */
public interface KeyStoreRepositoryIf {

    KeyStoreEntity findByName(String name);

    long countByName(String name);

    void save(KeyStoreEntity keyStoreEntity);
}
