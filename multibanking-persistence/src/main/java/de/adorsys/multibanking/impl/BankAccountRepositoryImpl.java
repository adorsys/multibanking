package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.repository.BankAccountRepositoryMongodb;
import de.adorsys.multibanking.repository.BankAccountRepositoryCustomMongodb;
import domain.BankAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankAccountRepositoryImpl implements BankAccountRepositoryIf {

    @Autowired
    private BankAccountRepositoryMongodb bankAccountRepository;

	@Autowired
	private BankAccountRepositoryCustomMongodb bankAccountRepositoryCustom;
	
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

	@Override
	public List<BankAccountEntity> deleteByBankAccess(String accessId) {
		return bankAccountRepository.deleteByBankAccessId(accessId);
	}

}
