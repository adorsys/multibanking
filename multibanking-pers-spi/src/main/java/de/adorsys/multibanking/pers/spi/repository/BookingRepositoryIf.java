package de.adorsys.multibanking.pers.spi.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.multibanking.domain.BookingEntity;
import domain.BankApi;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface BookingRepositoryIf {

    List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi);

    List<BookingEntity> findContracts(String userId, String bankAccountId, BankApi bankApi);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);

	void insert(List<BookingEntity> bookingEntities);

    List<BookingEntity> save(List<BookingEntity> bookingEntities);

    void deleteByAccountId(String id);
}
