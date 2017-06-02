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
        Query where = Query.query(Criteria.where("_id").is(new ObjectId(id)));

        where.fields().include("bankCode");

        return mongoTemplate.findOne(where, BankAccessEntity.class).getBankCode();
    }


}
