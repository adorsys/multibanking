package de.adorsys.multibanking.impl;

import de.adorsys.multibanking.domain.BookingsIndexEntity;
import de.adorsys.multibanking.pers.spi.repository.BookingsIndexRepositoryIf;
import de.adorsys.multibanking.repository.BookingsIndexRepositoryMongodb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Profile({"mongo", "fongo"})
@Service
public class BookingsIndexRepositoryImpl implements BookingsIndexRepositoryIf {

    @Autowired
    private BookingsIndexRepositoryMongodb repositoryMongodb;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(BookingsIndexEntity entity) {
        repositoryMongodb.save(entity);
    }

    @Override
    public void delete(BookingsIndexEntity entity) {
        repositoryMongodb.delete(entity);
    }

    @Override
    public List<BookingsIndexEntity> search(String text) {
        Collection<String> terms = new HashSet<>((Arrays.asList(text.split(" "))));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        return mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)), BookingsIndexEntity.class);
    }

    @Override
    public Optional<BookingsIndexEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return repositoryMongodb.findByUserIdAndAccountId(userId, accountId);
    }

}
