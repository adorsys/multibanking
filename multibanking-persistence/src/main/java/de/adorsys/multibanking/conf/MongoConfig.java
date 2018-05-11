package de.adorsys.multibanking.conf;

import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * Created by alexg on 08.02.17.
 */
@Configuration
@PropertySource(value = "${mongo.properties.url}", ignoreResourceNotFound = true)
@Profile({"mongo"})
public class MongoConfig extends AbstractMongoConfiguration {

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private MongoClient mongoClient;

    @Override
    protected String getDatabaseName() {
        return env.getProperty("mongo.databaseName");
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new ContinueOnBatchErrorTemplate(mongoDbFactory());
    }

    @Bean
    @Override
    public MongoClient mongoClient() {
        MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                .connectionsPerHost(50)
                .writeConcern(WriteConcern.JOURNALED)
                .readPreference(ReadPreference.secondaryPreferred())
                .build();

        ServerAddress serverAddress = getServerAddress();

        if (StringUtils.isEmpty(env.getProperty("mongo.userName"))) {
            return new MongoClient(serverAddress, mongoClientOptions);
        } else {
            MongoCredential mongoCredential = MongoCredential
                    .createCredential(env.getProperty("mongo.userName"), env.getProperty("mongo.databaseName"),
                            env.getProperty("mongo.password").toCharArray());

            return new MongoClient(serverAddress, mongoCredential, mongoClientOptions);
        }
    }

    private ServerAddress getServerAddress() {
        String[] serverParts = env.getProperty("mongo.server").replace("mongodb://", "").split(":");
        return new ServerAddress(serverParts[0],
                1 < serverParts.length ? Integer.valueOf(serverParts[1]) : ServerAddress.defaultPort());
    }

    public MongoDbFactory mongoDbFactory() {
        return new SimpleMongoDbFactory(mongoClient(), env.getProperty("mongo.databaseName"));
    }
}


