package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.jpa.entity.BookingJpaEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.BookingPageableRepositoryJpa;
import de.adorsys.multibanking.jpa.repository.BookingRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {

    private final BookingRepositoryJpa bookingRepository;
    private final BookingPageableRepositoryJpa bookingPageableRepositoryMongodb;

    private final JpaEntityMapper entityMapper;

    @Override
    public Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId,
                                                                          String bankAccountId, BankApi bankApi) {
        Page<BookingJpaEntity> bookingsPage =
            bookingPageableRepositoryMongodb.findByUserIdAndAccountIdAndBankApi(pageable, userId, bankAccountId,
                bankApi);

        return bookingsPage.map(entityMapper::mapToBookingEntity);
    }

    @Override
    public List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId,
                                                                  BankApi bankApi) {
        return entityMapper.mapToBookingEntities(bookingRepository.findByUserIdAndAccountIdAndBankApi(userId,
            bankAccountId, bankApi,
            new Sort(Sort.Direction.DESC, "valutaDate")));
    }

    @Override
    public Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId) {
        return bookingRepository.findByUserIdAndId(userId, bookingId)
            .map(entityMapper::mapToBookingEntity);
    }

    @Override
    public List<BookingEntity> findByUserIdAndIds(String name, List<String> ids) {
        return entityMapper.mapToBookingEntities(bookingRepository.findByUserIdAndIdIn(name, ids));
    }

    @Override
    public void save(List<BookingEntity> bookingEntities) {
        bookingEntities.stream()
            .filter(bookingEntity -> bookingEntity.getId() == null)
            .forEach(bookingEntity -> bookingEntity.setId(UUID.randomUUID().toString()));

        bookingRepository.saveAll(entityMapper.mapToBookingJpaEntities(bookingEntities));
    }

    @Override
    public void deleteByAccountId(String id) {
        bookingRepository.deleteByAccountId(id);
    }

}
