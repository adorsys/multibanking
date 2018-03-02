package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.pers.spi.repository.AnonymizedBookingRepositoryIf;
import de.adorsys.multibanking.repository.AnonymizdBookingRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class AnonymizedBookingRepositoryImpl implements AnonymizedBookingRepositoryIf {

	@Autowired
	private AnonymizdBookingRepositoryMongodb anonymizdBookingRepository;


	@Override
	public List<AnonymizedBookingEntity> save(List<AnonymizedBookingEntity> bookingEntities) {
		return anonymizdBookingRepository.save(bookingEntities);
	}
}
