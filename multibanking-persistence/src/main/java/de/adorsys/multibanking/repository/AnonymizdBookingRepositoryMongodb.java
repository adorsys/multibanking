package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo", "mongo-gridfs"})
public interface AnonymizdBookingRepositoryMongodb extends MongoRepository<AnonymizedBookingEntity, String> {

}
