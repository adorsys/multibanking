package de.adorsys.multibanking.repository.impl;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIF;
import de.adorsys.multibanking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class UserRepositoryImpl implements UserRepositoryIF {
	@Autowired
	private UserRepository userRepository;
	
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
