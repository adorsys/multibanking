package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.mongo.entity.BookingMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongo", "fongo"})
public interface BookingRepositoryMongodb extends MongoRepository<BookingMongoEntity, String> {

    List<BookingMongoEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi,
                                                                Sort sort);

    Optional<BookingMongoEntity> findByUserIdAndId(String userId, String bookingId);

    List<BookingMongoEntity> findByUserIdAndIdIn(String userId, List<String> bookingIds);

    void deleteByAccountId(String id);

}
