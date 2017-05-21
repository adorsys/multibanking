package de.adorsys.multibanking.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIF;
import de.adorsys.multibanking.repository.BankAccessRepository;

public class BankAccessRepositoryImpl implements BankAccessRepositoryIF {

    @Autowired
    BankAccessRepository bankAccessRepository;
	
	@Override
	public Optional<BankAccessEntity> findByUserIdAndId(String userId, String id) {
		return bankAccessRepository.findByUserIdAndId(userId, id);
	}

	@Override
	public List<BankAccessEntity> findByUserId(String userId) {
		return bankAccessRepository.findByUserId(userId);
	}

	@Override
	public BankAccessEntity save(BankAccessEntity bankAccess) {
		return bankAccessRepository.save(bankAccess);
	}

}
