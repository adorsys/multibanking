package de.adorsys.multibanking.jpa.impl;

import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.jpa.mapper.JpaEntityMapper;
import de.adorsys.multibanking.jpa.repository.ConsentRepositoryJpa;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"jpa"})
@Service
public class ConsentRepositoryImpl implements ConsentRepositoryIf {

    private final ConsentRepositoryJpa consentRepository;
    private final JpaEntityMapper entityMapper;

    @Override
    public Optional<ConsentEntity> findById(String id) {
        return consentRepository.findById(id)
            .map(entityMapper::toConsentEntity);
    }

    @Override
    public void save(ConsentEntity consentEntity) {
        consentRepository.save(entityMapper.toConsentJpaEntity(consentEntity));
    }

    @Override
    public void delete(ConsentEntity internalConsent) {
        consentRepository.deleteById(internalConsent.getId());
    }
}
