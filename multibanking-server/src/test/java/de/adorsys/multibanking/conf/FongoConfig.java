package de.adorsys.multibanking.conf;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.internal.build.MongoDriverVersion;
import de.adorsys.smartanalytics.config.EnableSmartanalyticsMongoPersistence;
import de.adorsys.sts.persistence.mongo.config.MongoConfiguration;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static java.lang.String.format;
import static java.lang.System.getProperty;

@Configuration
@EnableMongoRepositories(basePackages = "de.adorsys.multibanking.mongo.repository")
@EnableSmartanalyticsMongoPersistence
@Profile({"fongo"})
public class FongoConfig extends MongoConfiguration {

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(mongoClient));
        ((MappingMongoConverter) mongoTemplate.getConverter()).setMapKeyDotReplacement("#");
        return mongoTemplate;
    }

    @Bean
    public SimpleMongoClientDbFactory mongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDbFactory(mongoClient, "test");
    }

    @Bean(destroyMethod = "shutdown")
    public MongoServer mongoServer() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();
        return mongoServer;
    }

    @Bean(destroyMethod = "close")
    public MongoClient mongoClient() {

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoServer().getLocalAddress().toString()))
            .build();

        MongoDriverInformation driverInformation = MongoDriverInformation.builder().driverName(MongoDriverVersion.NAME)
            .driverVersion(MongoDriverVersion.VERSION)
            .driverPlatform(format("Java/%s/%s", getProperty("java.vendor", "unknown-vendor"),
                getProperty("java.runtime.version", "unknown-version"))).build();

        return new MongoClientImpl(settings, driverInformation);
    }
}
