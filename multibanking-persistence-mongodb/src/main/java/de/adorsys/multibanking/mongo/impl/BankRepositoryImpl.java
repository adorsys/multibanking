package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BankEntity;
import de.adorsys.multibanking.mongo.entity.BankMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BankRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BankRepositoryIf;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Profile({"mongo", "fongo"})
@Service
public class BankRepositoryImpl implements BankRepositoryIf {

    private final BankRepositoryMongodb bankRepositoryMongodb;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public Optional<BankEntity> findByBankCode(String blz) {
        return bankRepositoryMongodb.findByBankCode(blz)
            .map(entityMapper::mapToBankEntity);
    }

    @Override
    public void save(Iterable<BankEntity> bankEntities) {
        bankRepositoryMongodb.saveAll(entityMapper.mapToBankMongoEntities(bankEntities));
    }

    @Override
    public void deleteAll() {
        bankRepositoryMongodb.deleteAll();
    }

    @Override
    public void save(BankEntity bank) {
        bankRepositoryMongodb.save(entityMapper.mapToBankMongoEntity(bank));
    }

    @Override
    public List<BankEntity> search(String text) {
        Collection<String> terms = new HashSet<>((Arrays.asList(text.split(" "))));

        Criteria[] criterias = terms
            .stream()
            .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
            .toArray(Criteria[]::new);

        return entityMapper.mapToBankEntities(mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)),
            BankMongoEntity.class));
    }

}
