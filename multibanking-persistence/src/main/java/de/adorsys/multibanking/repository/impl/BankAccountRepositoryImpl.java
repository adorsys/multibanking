package de.adorsys.multibanking.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.multibanking.domain.BankAccountEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIF;
import de.adorsys.multibanking.repository.BankAccountRepository;

public class BankAccountRepositoryImpl implements BankAccountRepositoryIF {

    @Autowired
    private BankAccountRepository bankAccountRepository;
	
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

}
