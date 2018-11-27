package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo"})
public interface UserRepositoryMongodb extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findById(String id);

    @Query(fields = "{id : 1}")
    List<UserEntity> findByExpireUserLessThan(LocalDateTime date);
}
