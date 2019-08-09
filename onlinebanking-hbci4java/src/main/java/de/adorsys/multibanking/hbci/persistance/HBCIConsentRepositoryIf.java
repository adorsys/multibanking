package de.adorsys.multibanking.hbci.persistance;

import de.adorsys.multibanking.hbci.domain.HBCIConsentEntity;

import java.util.Optional;

public interface HBCIConsentRepositoryIf {

    Optional<HBCIConsentEntity> findById(String id);

    void save(HBCIConsentEntity consentEntity);

    void delete(HBCIConsentEntity internalConsent);

}
