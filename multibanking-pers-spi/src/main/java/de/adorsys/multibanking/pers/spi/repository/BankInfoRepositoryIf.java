package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankInfoEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BankInfoRepositoryIf {

    Optional<BankInfoEntity> findByBankCode(String userId);

}
