package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccountEntity;
import domain.BankAccount;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
public class BankAccountRepositoryCustom  {

    @Autowired
    private MongoTemplate mongoTemplate;

    public BankAccount.SyncStatus getSyncStatus(String accountId) {
        Query where = Query.query(Criteria.where("_id").is(new ObjectId(accountId)));

        where.fields().include("syncStatus");

        return mongoTemplate.findOne(where, BankAccountEntity.class).getSyncStatus();
    }

    public void updateSyncStatus(String accountId, BankAccount.SyncStatus syncStatus) {
        Query where = Query.query(Criteria.where("_id").is(new ObjectId(accountId)));
        Update update = new Update().set("syncStatus", syncStatus);
        mongoTemplate.updateFirst(where, update, BankAccountEntity.class);
    }
}
