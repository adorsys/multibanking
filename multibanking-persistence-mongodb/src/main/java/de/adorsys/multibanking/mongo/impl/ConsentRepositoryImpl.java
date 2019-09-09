package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.ConsentEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.ConsentRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.ConsentRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class ConsentRepositoryImpl implements ConsentRepositoryIf {

    private final ConsentRepositoryMongodb consentRepository;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<ConsentEntity> findByRedirectId(String id) {
        return consentRepository.findByRedirectId(id)
            .map(entityMapper::toConsentEntity);
    }

    @Override
    public Optional<ConsentEntity> findById(String id) {
        return consentRepository.findById(id)
            .map(entityMapper::toConsentEntity);
    }

    @Override
    public void save(ConsentEntity consentEntity) {
        consentRepository.save(entityMapper.toConsentMongoEntity(consentEntity));
    }

    @Override
    public void delete(ConsentEntity internalConsent) {
        consentRepository.deleteById(internalConsent.getId());
    }
}
