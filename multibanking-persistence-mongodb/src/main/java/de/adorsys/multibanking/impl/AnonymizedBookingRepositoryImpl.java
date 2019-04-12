package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.pers.spi.repository.AnonymizedBookingRepositoryIf;
import de.adorsys.multibanking.repository.AnonymizedBookingRepositoryMongodb;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class AnonymizedBookingRepositoryImpl implements AnonymizedBookingRepositoryIf {

    private final AnonymizedBookingRepositoryMongodb anonymizdBookingRepository;

    @Override
    public List<AnonymizedBookingEntity> save(List<AnonymizedBookingEntity> bookingEntities) {
        return anonymizdBookingRepository.saveAll(bookingEntities);
    }
}
