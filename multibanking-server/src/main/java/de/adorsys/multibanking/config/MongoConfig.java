package de.adorsys.multibanking.config;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.adorsys.smartanalytics.config.EnableSmartanalyticsMongoPersistence;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.Optional;

@AllArgsConstructor
@Profile({"mongo"})
@Configuration
@EnableMongoRepositories(basePackages = "de.adorsys.multibanking.mongo.repository")
@EnableSmartanalyticsMongoPersistence
@PropertySource(value = "${mongo.properties.url}", ignoreResourceNotFound = true)
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final Environment env;

    @Override
    protected @NonNull String getDatabaseName() {
        return Optional.ofNullable(env.getProperty("mongo.databaseName"))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.databaseName"));
    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter(mongoDbFactory(), customConversions(), mongoMappingContext(customConversions())));
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

    private ConnectionString getConnectionString() {
        return Optional.ofNullable(env.getProperty("mongo.server"))
            .map(server -> server.startsWith("mongodb://") ? server : "mongodb://" + server)
            .map(ConnectionString::new)
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.server"));
    }

    @Bean
    @Override
    public @NonNull MongoClient mongoClient() {
        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
            .applyConnectionString(getConnectionString())
            .writeConcern(WriteConcern.JOURNALED)
            .readPreference(ReadPreference.secondaryPreferred());

        if (StringUtils.isEmpty(env.getProperty("mongo.userName"))) {
            return MongoClients.create(mongoClientSettingsBuilder.build());
        } else {
            return MongoClients.create(mongoClientSettingsBuilder.credential(createMongoCredential()).build());
        }
    }

    @Deprecated
    @Bean
    public com.mongodb.MongoClient mongoClientOld() {
        MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
            .connectionsPerHost(50)
            .writeConcern(WriteConcern.JOURNALED)
            .readPreference(ReadPreference.secondaryPreferred())
            .build();

        ServerAddress serverAddress = Optional.ofNullable(env.getProperty("mongo.server"))
            .map(server -> server.replace("mongodb://", "").split(":"))
            .map(serverParts -> new ServerAddress(serverParts[0],
                1 < serverParts.length ? Integer.parseInt(serverParts[1]) : ServerAddress.defaultPort()))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.server"));

        if (StringUtils.isEmpty(env.getProperty("mongo.userName"))) {
            return new com.mongodb.MongoClient(serverAddress, mongoClientOptions);
        } else {
            MongoCredential mongoCredential = createMongoCredential();

            return new com.mongodb.MongoClient(serverAddress, Collections.singletonList(mongoCredential), mongoClientOptions);
        }
    }

    @Override
    public @NonNull SimpleMongoClientDbFactory mongoDbFactory() {
        return Optional.ofNullable(env.getProperty("mongo.databaseName"))
            .map(databaseName -> new SimpleMongoClientDbFactory(mongoClient(), databaseName))
            .orElseThrow(() -> new IllegalStateException("missing env property mongo.databaseName"));

    }

}


