package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.mongo.entity.BankAccountMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BankAccountRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    private final BankAccountRepositoryMongodb bankAccountRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public List<BankAccountEntity> findByUserId(String userId) {
        return entityMapper.mapToBankAccountEntities(bankAccountRepository.findByUserId(userId));
    }

    @Override
    public List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId) {
        return entityMapper.mapToBankAccountEntities(bankAccountRepository.findByUserIdAndBankAccessId(userId,
                bankAccessId));
    }

    @Override
    public Optional<BankAccountEntity> findByUserIdAndId(String userId, String id) {
        return bankAccountRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToBankAccountEntity);
    }

    @Override
    public boolean exists(String accountId) {
        return bankAccountRepository.existsById(accountId);
    }

    @Override
    public void save(List<BankAccountEntity> bankAccounts) {
        bankAccountRepository.saveAll(entityMapper.mapToBankAccountMongoEntities(bankAccounts));
    }

    @Override
    public void save(BankAccountEntity bankAccount) {
        bankAccountRepository.save(entityMapper.mapToBankAccountMongoEntity(bankAccount));
    }

    @Override
    public BankAccount.SyncStatus getSyncStatus(String accountId) {
        Query where = Query.query(Criteria.where("id").is(accountId));

        where.fields().include("syncStatus");
        BankAccountMongoEntity bankAccountEntity = mongoTemplate.findOne(where, BankAccountMongoEntity.class);
        if (bankAccountEntity != null) {
            return bankAccountEntity.getSyncStatus();
        }

        return null;
    }

    @Override
    public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
        Query where = Query.query(Criteria.where("id").is(accountId));
        Update update = new Update().set("syncStatus", syncStatus);
        mongoTemplate.updateFirst(where, update, BankAccountMongoEntity.class);
    }

    @Override
    public List<BankAccountEntity> deleteByBankAccess(String accessId) {
        return entityMapper.mapToBankAccountEntities(bankAccountRepository.deleteByBankAccessId(accessId));
    }

    @Override
    public Optional<BankAccountEntity> findOne(String accountId) {
        return bankAccountRepository.findById(accountId)
                .map(entityMapper::mapToBankAccountEntity);
    }

}
