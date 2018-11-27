package de.adorsys.multibanking.pers.spi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import de.adorsys.multibanking.domain.UserEntity;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface UserRepositoryIf {

    Optional<UserEntity> findById(String id);

	List<String> findExpiredUser();

	Optional<LocalDateTime> getRulesLastChangeDate(String id);

	Optional<UserEntity> setRulesLastChangeDate(String id, LocalDateTime dateTime);

	boolean exists(String userId);

	void save(UserEntity userEntity);

    void delete(String userId);
}
