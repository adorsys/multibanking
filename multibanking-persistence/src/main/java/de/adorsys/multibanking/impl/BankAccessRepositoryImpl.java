package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.repository.BankAccessRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Profile({"mongo", "fongo", "mongo-gridfs"})
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
    public BankAccessEntity findOne(String id) {
        return bankAccessRepository.findOne(id);
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
        Query where = Query.query(Criteria.where("id").is(id));

        BankAccessEntity found = mongoTemplate.findOne(where, BankAccessEntity.class);
        return found != null ? found.getBankCode() : null;
    }

    @Override
    public boolean exists(String accessId) {
        return bankAccessRepository.exists(accessId);
    }

    @Override
    public boolean deleteByUserIdAndBankAccessId(String userId, String bankAccessId) {
        Query where = Query.query(Criteria.where("id").is(bankAccessId).and("userId").is(userId));
        return mongoTemplate.remove(where, BankAccessEntity.class).getN() > 0;
    }

}
