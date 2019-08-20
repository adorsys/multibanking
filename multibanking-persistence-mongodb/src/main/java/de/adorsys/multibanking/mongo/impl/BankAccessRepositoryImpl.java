package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.mongo.entity.BankAccessMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BankAccessRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BankAccessRepositoryImpl implements BankAccessRepositoryIf {

    private final BankAccessRepositoryMongodb bankAccessRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<BankAccessEntity> findByUserIdAndId(String userId, String id) {
        return bankAccessRepository.findByUserIdAndId(userId, id)
                .map(entityMapper::mapToBankAccessEntity);
    }

    @Override
    public Optional<BankAccessEntity> findOne(String id) {
        return bankAccessRepository.findById(id)
                .map(entityMapper::mapToBankAccessEntity);
    }

    @Override
    public List<BankAccessEntity> findByUserId(String userId) {
        return entityMapper.mapToBankAccessEntities(bankAccessRepository.findByUserId(userId));
    }

    @Override
    public Optional<BankAccessEntity> findByConsentId(String consentId) {
        return bankAccessRepository.findByConsentId(consentId)
            .map(entityMapper::mapToBankAccessEntity);
    }

    @Override
    public void save(BankAccessEntity bankAccess) {
        BankAccessMongoEntity bankAccessMongoEntity = bankAccessRepository.save(entityMapper.mapToBankAccessMongoEntity(bankAccess));
        bankAccess.setId(bankAccessMongoEntity.getId());
    }

    @Override
    public String getBankCode(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        query.fields().include("bankCode");

        return Optional.ofNullable(mongoTemplate.findOne(query, BankAccessMongoEntity.class))
                .map(BankAccess::getBankCode)
                .orElse(null);
    }

    @Override
    public boolean exists(String accessId) {
        return bankAccessRepository.existsById(accessId);
    }

    @Override
    public boolean deleteByUserIdAndBankAccessId(String userId, String id) {
        return bankAccessRepository.deleteByUserIdAndId(userId, id) > 0;
    }
}
