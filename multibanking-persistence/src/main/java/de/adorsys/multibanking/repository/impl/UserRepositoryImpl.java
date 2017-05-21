package de.adorsys.multibanking.repository.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIF;
import de.adorsys.multibanking.repository.UserRepository;

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
