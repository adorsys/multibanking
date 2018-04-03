package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import de.adorsys.multibanking.repository.UserRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class UserRepositoryImpl implements UserRepositoryIf {

    @Autowired
    private UserRepositoryMongodb userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<UserEntity> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public List<String> findExpiredUser() {
        Query query = new Query(
                Criteria.where("expireUser").lte(new Date())
        );
        query.fields().include("id");

        return mongoTemplate.find(query, UserEntity.class)
                .stream()
                .map(userEntity -> userEntity.getId())
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String userId) {
        return userRepository.exists(userId);
    }

    @Override
    public void save(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    @Override
    public void delete(String userId) {
        userRepository.delete(userId);
    }

}
