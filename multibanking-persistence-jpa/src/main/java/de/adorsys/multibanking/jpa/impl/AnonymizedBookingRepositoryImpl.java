package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.AnonymizedBookingEntity;
import de.adorsys.multibanking.jpa.repository.AnonymizedBookingRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.AnonymizedBookingRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class AnonymizedBookingRepositoryImpl implements AnonymizedBookingRepositoryIf {

    private final AnonymizedBookingRepositoryJpa anonymizdBookingRepository;

    @Override
    public void save(List<AnonymizedBookingEntity> bookingEntities) {

    }
}
