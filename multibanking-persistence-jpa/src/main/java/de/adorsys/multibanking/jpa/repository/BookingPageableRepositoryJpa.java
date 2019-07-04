package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.jpa.entity.BookingJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"jpa"})
public interface BookingPageableRepositoryJpa extends JpaRepository<BookingJpaEntity, String> {

    Page<BookingJpaEntity> findByUserIdAndAccountIdAndBankApi(Pageable pageable, String userId, String bankAccountId,
                                                              BankApi bankApi);

}
