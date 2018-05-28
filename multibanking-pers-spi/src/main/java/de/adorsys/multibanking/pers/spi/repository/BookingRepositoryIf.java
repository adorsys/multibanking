package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BookingEntity;
import domain.BankApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BookingRepositoryIf {

    Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId, String bankAccountId, BankApi bankApi);

    List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);

    Iterable<BookingEntity> findByUserIdAndIds(String name, List<String> ids);

    List<BookingEntity> save(List<BookingEntity> bookingEntities);

    void deleteByAccountId(String id);
}
