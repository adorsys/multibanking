package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 17.07.17
 */
public interface BankRepositoryIf {

    Optional<BankEntity> findByBankCode(String bankCode);

    void save(BankEntity bank);

    void save(Iterable<BankEntity> bankEntities);

    void deleteAll();

    List<BankEntity> search(String terms);
}
