package de.adorsys.multibanking.conf;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.adorsys.smartanalytics.config.EnableSmartanalyticsMongoPersistence;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "de.adorsys.multibanking.mongo.repository")
@EnableSmartanalyticsMongoPersistence
@Profile({"fongo"})
public class FongoConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "multibanking";
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(mongoClient));
        ((MappingMongoConverter) mongoTemplate.getConverter()).setMapKeyDotReplacement("#");
        return mongoTemplate;
    }

    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoDbFactory(mongoClient, "test");
    }

    @Bean(destroyMethod = "shutdown")
    public MongoServer mongoServer() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();
        return mongoServer;
    }

    @Bean(destroyMethod = "close")
    public MongoClient mongoClient() {
        return new MongoClient(new ServerAddress(mongoServer().getLocalAddress()));
    }
}
