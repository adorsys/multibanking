package de.adorsys.multibanking.mongo.impl;

import de.adorsys.multibanking.domain.BookingsIndexEntity;
import de.adorsys.multibanking.mongo.entity.BookingsIndexMongoEntity;
import de.adorsys.multibanking.mongo.mapper.MongoEntityMapper;
import de.adorsys.multibanking.mongo.repository.BookingsIndexRepositoryMongodb;
import de.adorsys.multibanking.pers.spi.repository.BookingsIndexRepositoryIf;
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
public class BookingsIndexRepositoryImpl implements BookingsIndexRepositoryIf {

    private final BookingsIndexRepositoryMongodb repositoryMongodb;
    private final MongoTemplate mongoTemplate;
    private final MongoEntityMapper entityMapper;

    @Override
    public void save(BookingsIndexEntity entity) {
        Query query = new Query(
                Criteria.where("userId").is(entity.getUserId()).and("accountId").is(entity.getAccountId())
        );
        mongoTemplate.remove(query, BookingsIndexMongoEntity.class);
        repositoryMongodb.save(entityMapper.mapToBookingsIndexMongoEntity(entity));
    }

    @Override
    public void delete(BookingsIndexEntity entity) {
        repositoryMongodb.delete(entityMapper.mapToBookingsIndexMongoEntity(entity));
    }

    @Override
    public List<BookingsIndexEntity> search(String text) {
        Collection<String> terms = new HashSet<>((Arrays.asList(text.split(" "))));

        Criteria[] criterias = terms
                .stream()
                .map(s -> Criteria.where("searchIndex").regex(s.toLowerCase(), "iu"))
                .toArray(Criteria[]::new);

        return entityMapper.mapToBookingsIndexEntities(mongoTemplate.find(Query.query(new Criteria().andOperator(criterias)), BookingsIndexMongoEntity.class));
    }

    @Override
    public Optional<BookingsIndexEntity> findByUserIdAndAccountId(String userId, String accountId) {
        return repositoryMongodb.findByUserIdAndAccountId(userId, accountId)
                .map(entityMapper::mapToBookingsIndexEntity);
    }

}
