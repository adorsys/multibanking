package de.adorsys.multibanking.mongo.repository;

import de.adorsys.multibanking.mongo.entity.AnonymizedBookingMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"mongo", "fongo"})
public interface AnonymizedBookingRepositoryMongodb extends MongoRepository<AnonymizedBookingMongoEntity, String> {

}
