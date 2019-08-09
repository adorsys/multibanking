package de.adorsys.multibanking.hbci.persistance;

import de.adorsys.multibanking.hbci.domain.HBCIConsentEntity;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class HbciConsentRepositoryImpl implements HBCIConsentRepositoryIf {

    private static HBCIConsentRepositoryIf instance;
    private Map<String, HBCIConsentEntity> inMemory = new HashMap<>();

    private HbciConsentRepositoryImpl() {

    }

    public static HBCIConsentRepositoryIf getInstance() {
        if (instance == null) {
            instance = new HbciConsentRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Optional<HBCIConsentEntity> findById(String id) {
        HBCIConsentEntity output = inMemory.get(id);
        return output == null ? Optional.empty() : Optional.of(output);
    }

    @Override
    public void save(HBCIConsentEntity consentEntity) {
        if (consentEntity.getId() == null) {
            do {
                consentEntity.setId(UUID.randomUUID().toString());
            } while (inMemory.get(consentEntity.getId()) != null);
        }
        inMemory.put(consentEntity.getId(), consentEntity);
    }

    @Override
    public void delete(HBCIConsentEntity internalConsent) {
        inMemory.remove(internalConsent.getId());
    }
}
