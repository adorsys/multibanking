package de.adorsys.multibanking.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import de.adorsys.multibanking.domain.MlAnonymizedBookingEntity;


@Repository
@Profile({"mongo", "fongo"})
public interface MlAnonymizedBookingRepositoryMongodb extends MongoRepository<MlAnonymizedBookingEntity, String>{
	
	List<MlAnonymizedBookingEntity> findByUserId(String userId);
	
	long deleteByUserId(String userId);
}
