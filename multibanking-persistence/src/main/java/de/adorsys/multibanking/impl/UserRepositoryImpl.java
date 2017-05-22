package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.repository.UserRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class UserRepositoryImpl implements UserRepositoryIf {

	@Autowired
	private UserRepositoryMongodb userRepository;
	
	@Override
	public Optional<UserEntity> findById(String id) {
		return userRepository.findById(id);
	}

	@Override
	public boolean exists(String userId) {
		return userRepository.exists(userId);
	}

	@Override
	public void save(UserEntity userEntity) {
		userRepository.save(userEntity);
	}

}
