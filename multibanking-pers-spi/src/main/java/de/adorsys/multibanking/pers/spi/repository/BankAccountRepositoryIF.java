package de.adorsys.multibanking.pers.spi.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.multibanking.domain.BankAccountEntity;
import domain.BankAccount;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BankAccountRepositoryIF {

    List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId);

    Optional<BankAccountEntity> findByUserIdAndId(String userId, String id);

	boolean exists(String accountId);

	void save(List<BankAccountEntity> bankAccounts);

	void save(BankAccountEntity bankAccount);

	public BankAccount.SyncStatus getSyncStatus(String accountId);

	public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus);
}
