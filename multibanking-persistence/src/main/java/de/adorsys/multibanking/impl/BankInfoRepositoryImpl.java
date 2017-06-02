package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankInfoEntity;
import de.adorsys.multibanking.pers.spi.repository.BankInfoRepositoryIf;
import de.adorsys.multibanking.repository.BankInfoRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankInfoRepositoryImpl implements BankInfoRepositoryIf {

    @Autowired
	BankInfoRepositoryMongodb bankInfoRepository;

	@Override
	public Optional<BankInfoEntity> findByBankCode(String blz) {
		return bankInfoRepository.findByBankCode(blz);
	}
}
