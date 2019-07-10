package de.adorsys.multibanking.config;

import com.mongodb.*;
import de.adorsys.smartanalytics.config.EnableSmartanalyticsMongoPersistence;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo"})
@Configuration
@EnableMongoRepositories(basePackages = "de.adorsys.multibanking.mongo.repository")
@EnableSmartanalyticsMongoPersistence
@PropertySource(value = "${mongo.properties.url}", ignoreResourceNotFound = true)
public class MongoConfig extends AbstractMongoConfiguration {

    private final Environment env;

    @Override
    protected String getDatabaseName() {
        return Optional.ofNullable(env.getProperty("mongo.databaseName"))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.databaseName"));
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
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
            MongoCredential mongoCredential = createMongoCredential();

            return new MongoClient(serverAddress, mongoCredential, mongoClientOptions);
        }
    }

    private MongoCredential createMongoCredential() {
        String userName = Optional.ofNullable(env.getProperty("mongo.userName"))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.userName"));

        String databaseName = Optional.ofNullable(env.getProperty("mongo.databaseName"))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.databaseName"));

        String password = Optional.ofNullable(env.getProperty("mongo.password"))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.password"));

        return MongoCredential.createCredential(userName, databaseName,
            password.toCharArray());
    }

    private ServerAddress getServerAddress() {
        return Optional.ofNullable(env.getProperty("mongo.server"))
            .map(server -> server.replace("mongodb://", "").split(":"))
            .map(serverParts -> new ServerAddress(serverParts[0],
                1 < serverParts.length ? Integer.valueOf(serverParts[1]) : ServerAddress.defaultPort()))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.server"));
    }

    @Override
    public MongoDbFactory mongoDbFactory() {
        return Optional.ofNullable(env.getProperty("mongo.databaseName"))
            .map(databaseName -> new SimpleMongoDbFactory(mongoClient(), databaseName))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.databaseName"));

    }
}


