package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import de.adorsys.multibanking.repository.BankRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Profile({"mongo", "fongo", "mongo-gridfs"})
@Service
public class BankRepositoryImpl implements BankRepositoryIf {

    @Autowired
    private BankRepositoryMongodb bankRepositoryMongodb;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<BankEntity> findByBankCode(String blz) {
        return bankRepositoryMongodb.findByBankCode(blz);
    }

    @Override
    public void save(BankEntity bank) {
        bankRepositoryMongodb.save(bank);
    }

    @Override
    public List<BankEntity> search(String text) {
        Collection<String> terms = new HashSet<>((Arrays.asList(text.split(" "))));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        List<BankEntity> bankEntities = mongoTemplate.find(Query.query( new Criteria().andOperator(criterias)), BankEntity.class);

        return bankEntities;
    }


}
