package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.LockEntity;
import de.adorsys.multibanking.pers.spi.repository.LockRepositoryIf;
import de.adorsys.multibanking.repository.LockRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile({"mongo", "fongo"})
@Service
public class LockRepositoryImpl implements LockRepositoryIf {

    @Autowired
    private LockRepositoryMongodb lockRepositoryMongodb;

    @Override
    public List<LockEntity> findAll() {
        return lockRepositoryMongodb.findAll();
    }

    @Override
    public LockEntity findByName(String name) {
        return lockRepositoryMongodb.findByName(name);
    }

    @Override
    public void save(LockEntity lockEntity) {
        lockRepositoryMongodb.save(lockEntity);
    }

    @Override
    public void deleteByName(String name) {
        lockRepositoryMongodb.deleteByName(name);
    }

    @Override
    public LockEntity findByNameAndValue(String name, String value) {
        return lockRepositoryMongodb.findByNameAndValue(name, value);
    }
}
