package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.mongo.entity.BookingMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BookingPageableRepositoryMongodb;
import de.adorsys.multibanking.mongo.repository.BookingRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BookingRepositoryImpl implements BookingRepositoryIf {

    private final BookingRepositoryMongodb bookingRepository;
    private final BookingPageableRepositoryMongodb bookingPageableRepositoryMongodb;
    private final MongoEntityMapper entityMapper;

    @Override
    public Page<BookingEntity> findPageableByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId,
                                                                          String bankAccountId, BankApi bankApi) {
        Page<BookingMongoEntity> bookingsPage =
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
        return entityMapper.mapToBookingEntities(bookingRepository.findByUserIdAndId(name, ids));

    }

    @Override
    public void save(List<BookingEntity> bookingEntities) {
        List<BookingEntity> newEntities = bookingEntities
                .stream()
                .filter(bookingEntity -> bookingEntity.getId() == null)
                .peek(bookingEntity -> bookingEntity.setId(UUID.randomUUID().toString()))
                .collect(Collectors.toList());

        List<BookingEntity> existingEntities = bookingEntities
                .stream()
                .filter(bookingEntity -> bookingEntity.getId() != null)
                .collect(Collectors.toList());
        try {
            bookingRepository.insert(entityMapper.mapToBookingMongoEntities(newEntities));
        } catch (DuplicateKeyException e) {
            //ignore it
        }

        bookingRepository.saveAll(entityMapper.mapToBookingMongoEntities(existingEntities));
    }

    @Override
    public void deleteByAccountId(String id) {
        bookingRepository.deleteByAccountId(id);
    }

    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        bookingRepository.deleteByUserIdAndAccountId(userId, accountId);
    }

}
