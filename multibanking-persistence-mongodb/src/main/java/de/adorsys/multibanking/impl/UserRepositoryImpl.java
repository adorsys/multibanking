package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.repository.UserRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<String> findExpiredUser() {
        return userRepository.findByExpireUserLessThan(LocalDateTime.now())
                .stream()
                .map(userEntity -> userEntity.getId())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LocalDateTime> getRulesLastChangeDate(String id) {
        return findById(id)
                .flatMap(userEntity -> Optional.ofNullable(userEntity.getRulesLastChangeDate()));
    }

    @Override
    public Optional<UserEntity> setRulesLastChangeDate(String id, LocalDateTime dateTime) {
        return findById(id)
                .flatMap(userEntity -> {
                    userEntity.setRulesLastChangeDate(dateTime);
                    return Optional.of(userRepository.save(userEntity));
                });
    }

    @Override
    public boolean exists(String userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public void save(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    @Override
    public void delete(String userId) {
        userRepository.deleteById(userId);
    }

}
