package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankEntity;
import domain.Bank;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 17.07.17
 */
public interface BankRepositoryIf {

    Optional<BankEntity> findByBankCode(String bankCode);

    void save(BankEntity bank);

    List<BankEntity> search(String terms);
}
