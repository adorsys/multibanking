package de.adorsys.multibanking.pers.spi.repository;

import java.util.Optional;

import de.adorsys.multibanking.domain.UserEntity;

/**
 * @author alexg on 07.02.17
 * @author fpo on 21.05.2017
 */
public interface UserRepositoryIF {

    Optional<UserEntity> findById(String id);

	boolean exists(String userId);

	void save(UserEntity userEntity);
}
