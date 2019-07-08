package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.jpa.entity.BankAccountJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface BankAccountRepositoryJpa extends JpaRepository<BankAccountJpaEntity, Long> {

    List<BankAccountJpaEntity> findByUserId(String userId);

    List<BankAccountJpaEntity> findByUserIdAndBankAccessId(String userId, String bankAccessId);

    Optional<BankAccountJpaEntity> findByUserIdAndId(String userId, Long id);

    List<BankAccountJpaEntity> deleteByBankAccessId(String accessId);

    @Query(value = "SELECT sync_status FROM bank_account where id = ?1", nativeQuery = true)
    BankAccount.SyncStatus getSyncStatus(Long id);

    @Modifying
    @Query("update bank_account account set account.syncStatus = ?1 where account.id = ?2")
    int updateSyncStatus(BankAccount.SyncStatus status, Long id);

}
