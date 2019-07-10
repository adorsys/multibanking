package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.jpa.entity.BookingJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BookingRepositoryJpa extends JpaRepository<BookingJpaEntity, String> {

    List<BookingJpaEntity> findByUserIdAndAccountIdAndBankApi(String userId, String bankAccountId, BankApi bankApi,
                                                              Sort sort);

    Optional<BookingJpaEntity> findByUserIdAndId(String userId, String bookingId);

    List<BookingJpaEntity> findByUserIdAndIdIn(String userId, List<String> bookingIds);

    void deleteByAccountId(String id);

    void deleteByUserIdAndAccountId(String userId, String accountId);
}
