package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookingRepositoryIf {

    Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId,
                                                                   String bankAccountId, BankApi bankApi);

    List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);

    Iterable<BookingEntity> findByUserIdAndIds(String name, List<String> ids);

    void save(List<BookingEntity> bookingEntities);

    void deleteByAccountId(String id);

}
