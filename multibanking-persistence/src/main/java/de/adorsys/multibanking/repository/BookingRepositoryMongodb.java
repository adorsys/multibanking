package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BookingEntity;
import domain.BankApi;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface BookingRepositoryMongodb extends MongoRepository<BookingEntity, String> {

    List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi, Sort sort);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);

    void deleteByAccountId(String id);
}
