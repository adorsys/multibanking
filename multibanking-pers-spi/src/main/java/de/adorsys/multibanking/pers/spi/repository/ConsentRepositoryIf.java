package de.adorsys.multibanking.pers.spi.repository;

import de.adorsys.multibanking.domain.ConsentEntity;

import java.util.Optional;

public interface ConsentRepositoryIf {

    Optional<ConsentEntity> findById(String id);

    void save(ConsentEntity consentEntity);

    void delete(ConsentEntity internalConsent);

}
