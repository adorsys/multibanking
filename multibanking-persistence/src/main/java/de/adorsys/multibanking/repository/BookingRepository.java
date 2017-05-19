package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BookingEntity;
import domain.BankApi;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
public interface BookingRepository extends MongoRepository<BookingEntity, String> {

    List<BookingEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);


}
