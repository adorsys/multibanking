package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BookingEntity;
import domain.BankApi;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo"})
public interface BookingPageableRepositoryMongodb extends MongoRepository<BookingEntity, String> {

    Page<BookingEntity> findByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId, String bankAccountId, BankApi bankApi);

}
