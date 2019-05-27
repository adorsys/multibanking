package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.MlAnonymizedBookingMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"mongo", "fongo"})
public interface MlAnonymizedBookingRepositoryMongodb extends MongoRepository<MlAnonymizedBookingMongoEntity, String> {

    List<MlAnonymizedBookingMongoEntity> findByUserId(String userId);

    long deleteByUserId(String userId);
}
