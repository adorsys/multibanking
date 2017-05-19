package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BookingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
public interface BookingRepository extends MongoRepository<BookingEntity, String> {

    List<BookingEntity> findByUserIdAndAccountId(String userId, String bankAccountId);

    Optional<BookingEntity> findByUserIdAndId(String userId, String bookingId);


}
