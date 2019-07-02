package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BankAccountRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    private final BankAccountRepositoryJpa bankAccountRepository;
    private final JpaEntityMapper entityMapper;

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
        return bankAccountRepository.findByUserIdAndId(userId, new Long(id))
                .map(entityMapper::mapToBankAccountEntity);
    }

    @Override
    public boolean exists(String accountId) {
        return bankAccountRepository.existsById(new Long(accountId));
    }

    @Override
    public void save(List<BankAccountEntity> bankAccounts) {
        bankAccountRepository.saveAll(entityMapper.mapToBankAccountJpaEntities(bankAccounts));
    }

    @Override
    public void save(BankAccountEntity bankAccount) {
        bankAccount.setId(bankAccountRepository.save(entityMapper.mapToBankAccountJpaEntity(bankAccount)).getId().toString());
    }

    @Override
    public BankAccount.SyncStatus getSyncStatus(String accountId) {
//        Query where = Query.query(Criteria.where("id").is(accountId));
//
//        where.fields().include("syncStatus");
//        BankAccountMongoEntity bankAccountEntity = mongoTemplate.findOne(where, BankAccountMongoEntity.class);
//        if (bankAccountEntity != null) {
//            return bankAccountEntity.getSyncStatus();
//        }

        return null;
    }

    @Override
    public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
//        Query where = Query.query(Criteria.where("id").is(accountId));
//        Update update = new Update().set("syncStatus", syncStatus);
//        mongoTemplate.updateFirst(where, update, BankAccountMongoEntity.class);
    }

    @Override
    public List<BankAccountEntity> deleteByBankAccess(String accessId) {
        return entityMapper.mapToBankAccountEntities(bankAccountRepository.deleteByBankAccessId(accessId));
    }

    @Override
    public Optional<BankAccountEntity> findOne(String accountId) {
        return bankAccountRepository.findById(new Long(accountId))
                .map(entityMapper::mapToBankAccountEntity);
    }
}
