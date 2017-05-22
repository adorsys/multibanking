package de.adorsys.multibanking.repository.impl;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIF;
import de.adorsys.multibanking.repository.BankAccountRepository;
import de.adorsys.multibanking.repository.BankAccountRepositoryCustom;
import domain.BankAccount;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class BankAccountRepositoryImpl implements BankAccountRepositoryIF {

    @Autowired
    private BankAccountRepository bankAccountRepository;

	@Autowired
	private BankAccountRepositoryCustom bankAccountRepositoryCustom;
	
	@Override
	public List<BankAccountEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId) {
		return bankAccountRepository.findByUserIdAndBankAccessId(userId, bankAccessId);
	}

	@Override
	public Optional<BankAccountEntity> findByUserIdAndId(String userId, String id) {
		return bankAccountRepository.findByUserIdAndId(userId, id);
	}

	@Override
	public boolean exists(String accountId) {
		return bankAccountRepository.exists(accountId);
	}

	@Override
	public void save(List<BankAccountEntity> bankAccounts) {
		bankAccountRepository.save(bankAccounts);
	}

	@Override
	public void save(BankAccountEntity bankAccount) {
		bankAccountRepository.save(bankAccount);
	}

	@Override
	public BankAccount.SyncStatus getSyncStatus(String accountId) {
		return bankAccountRepositoryCustom.getSyncStatus(accountId);
	}

	@Override
	public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
		bankAccountRepositoryCustom.updateSyncStatus(accountId, syncStatus);
	}

}
