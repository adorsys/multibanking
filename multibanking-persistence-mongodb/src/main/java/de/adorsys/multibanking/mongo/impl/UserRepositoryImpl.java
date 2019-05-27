package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.UserEntity;
import de.adorsys.multibanking.mongo.entity.UserMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.UserRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.UserRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class UserRepositoryImpl implements UserRepositoryIf {

    private final UserRepositoryMongodb userRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<UserEntity> findById(String id) {
        return userRepository.findById(id).map(entityMapper::mapToUserEntity);
    }

    @Override
    public List<String> findExpiredUser() {
        return userRepository.findByExpireUserLessThan(LocalDateTime.now())
                .stream()
                .map(UserMongoEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LocalDateTime> getRulesLastChangeDate(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        query.fields().include("rulesLastChangeDate");

        return Optional.ofNullable(mongoTemplate.findOne(query, UserMongoEntity.class))
                .map(UserMongoEntity::getRulesLastChangeDate);
    }

    @Override
    public void setRulesLastChangeDate(String userId, LocalDateTime dateTime) {
        Query where = Query.query(Criteria.where("id").is(userId));
        Update update = new Update().set("rulesLastChangeDate", dateTime);
        mongoTemplate.updateFirst(where, update, UserMongoEntity.class);
    }

    @Override
    public boolean exists(String userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public void save(UserEntity userEntity) {
        userRepository.save(entityMapper.mapToUserMongoEntity(userEntity));
    }

    @Override
    public void delete(String userId) {
        userRepository.deleteById(userId);
    }

}
