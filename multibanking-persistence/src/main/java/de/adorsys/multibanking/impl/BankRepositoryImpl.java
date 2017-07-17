package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.repository.BankRepositoryMongodb;
import domain.Bank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankRepositoryImpl implements BankRepositoryIf {

    @Autowired
	BankRepositoryMongodb bankRepositoryMongodb;

	@Override
	public Optional<BankEntity> findByBankCode(String blz) {
		return bankRepositoryMongodb.findByBankCode(blz);
	}

	@Override
	public void save(BankEntity bank) {
		bankRepositoryMongodb.save(bank);
	}

	@Override
	public List<BankEntity> findAll() {
		return bankRepositoryMongodb.findAll();
	}
}
