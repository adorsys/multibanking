package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.domain.BookingsIndexEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 13.07.18
 */
public interface BookingsIndexRepositoryIf {

    void save(BookingsIndexEntity entity);

    void delete(BookingsIndexEntity entity);

    List<BookingsIndexEntity> search(String terms);

    Optional<BookingsIndexEntity> findByUserIdAndAccountId(String userId, String accountId);
}
