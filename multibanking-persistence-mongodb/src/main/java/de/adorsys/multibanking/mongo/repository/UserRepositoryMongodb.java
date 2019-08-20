package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.UserMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile({"mongo", "fongo"})
public interface UserRepositoryMongodb extends MongoRepository<UserMongoEntity, String> {

    @Query(fields = "{id : 1}")
    List<UserMongoEntity> findByExpireUserLessThan(LocalDateTime date);
}
