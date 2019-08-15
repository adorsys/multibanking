package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BankAccountRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
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
    public List<BankAccountEntity> save(List<BankAccountEntity> bankAccounts) {
        return entityMapper.mapToBankAccountEntities(bankAccountRepository.saveAll(entityMapper.mapToBankAccountJpaEntities(bankAccounts)));
    }

    @Override
    public void save(BankAccountEntity bankAccount) {
        bankAccount.setId(bankAccountRepository.save(entityMapper.mapToBankAccountJpaEntity(bankAccount)).getId().toString());
    }

    @Override
    public BankAccount.SyncStatus getSyncStatus(String accountId) {
        return bankAccountRepository.getSyncStatus(NumberUtils.toLong(accountId));
    }

    @Override
    public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
        bankAccountRepository.updateSyncStatus(syncStatus, NumberUtils.toLong(accountId));
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
