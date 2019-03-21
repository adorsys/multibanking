package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.repository.BankAccessRepositoryMongodb;
import domain.BankAccess;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo"})
@Service
public class BankAccessRepositoryImpl implements BankAccessRepositoryIf {

    @Autowired
    private BankAccessRepositoryMongodb bankAccessRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<BankAccessEntity> findByUserIdAndId(String userId, String id) {
        return bankAccessRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public Optional<BankAccessEntity> findOne(String id) {
        return bankAccessRepository.findById(id);
    }

    @Override
    public List<BankAccessEntity> findByUserId(String userId) {
        return bankAccessRepository.findByUserId(userId);
    }

    @Override
    public BankAccessEntity save(BankAccessEntity bankAccess) {
        return bankAccessRepository.save(bankAccess);
    }

    @Override
    public String getBankCode(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        query.fields().include("bankCode");

        return Optional.ofNullable(mongoTemplate.findOne(query, BankAccessEntity.class))
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

    @Value
    class BankCodeOnly {
        String bankCode;
    }
}
