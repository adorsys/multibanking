package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.repository.BankAccessRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankAccessRepositoryImpl implements BankAccessRepositoryIf {

    @Autowired
    BankAccessRepositoryMongodb bankAccessRepository;
	
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
