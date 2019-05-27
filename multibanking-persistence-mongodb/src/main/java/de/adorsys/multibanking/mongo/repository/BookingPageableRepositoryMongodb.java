package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.mongo.entity.BookingMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"mongo", "fongo"})
public interface BookingPageableRepositoryMongodb extends MongoRepository<BookingMongoEntity, String> {

    Page<BookingMongoEntity> findByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId, String bankAccountId,
                                                                BankApi bankApi);

}
