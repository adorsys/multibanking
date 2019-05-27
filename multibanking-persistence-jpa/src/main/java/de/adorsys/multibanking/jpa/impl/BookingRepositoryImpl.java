package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.jpa.repository.BookingPageableRepositoryJpa;
import de.adorsys.multibanking.jpa.repository.BookingRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {

    private final BookingRepositoryJpa bookingRepository;
    private final BookingPageableRepositoryJpa bookingPageableRepositoryMongodb;

    @Override
    public Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId,
                                                                          String bankAccountId, BankApi bankApi) {
        return null;
    }

    @Override
    public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
                                                                  BankApi bankApi) {
        return null;
    }

    @Override
    public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
        return Optional.empty();
    }

    @Override
    public Iterable<BookingEntity> findByUserIdAndIds(String name, List<String> ids) {
        return null;
    }

    @Override
    public void save(List<BookingEntity> bookingEntities) {
    }

    @Override
    public void deleteByAccountId(String id) {

    }

    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {

    }
}
