package de.adorsys.multibanking.repository;

import de.adorsys.multibanking.domain.BankAccessEntity;
import de.adorsys.multibanking.domain.BankAccountEntity;
import domain.BankAccount;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * Created by alexg on 07.02.17.
 */
@Repository
@Profile({"mongo", "fongo"})
public class BankAccessRepositoryCustomMongodb {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getBankCode(String id) {
        Query where = Query.query(Criteria.where("id").is(id));

        where.fields().include("bankCode");

        // Francis null pointer when noting is found
        BankAccessEntity found = mongoTemplate.findOne(where, BankAccessEntity.class);
        return found!=null?found.getBankCode():null;
    }

    public boolean deleteByUserIdAndId(String userId, String bankAccessId) {
        Query where = Query.query(Criteria.where("id").is(bankAccessId).and("userId").is(userId));
        return mongoTemplate.remove(where, BankAccessEntity.class).getN() > 0;
    }




}
