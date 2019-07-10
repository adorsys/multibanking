package de.adorsys.multibanking.jpa.repository;

import de.adorsys.multibanking.jpa.entity.UserJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"jpa"})
public interface UserRepositoryJpa extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findById(String id);

    List<UserJpaEntity> findByExpireUserLessThan(LocalDateTime date);

    @Query(value = "SELECT rules_last_change_date FROM mbs_user WHERE id = ?1", nativeQuery = true)
    LocalDateTime getRulesLastChangeDate(String userId);

    @Modifying
    @Query("update mbs_user user set user.rulesLastChangeDate = ?1 where user.id = ?2")
    int setRulesLastChangeDate(LocalDateTime changeDate, String id);

}
