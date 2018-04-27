package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.LockEntity;

import java.util.List;

public interface LockRepositoryIf {
    List<LockEntity> findAll();

    LockEntity findByName(String name);

    void save(LockEntity lockEntity);

    void deleteByName(String name);

    LockEntity findByNameAndValue(String name, String value);
}
