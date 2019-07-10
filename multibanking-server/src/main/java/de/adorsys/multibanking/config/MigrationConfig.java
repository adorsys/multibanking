package de.adorsys.multibanking.config;

import com.github.mongobee.Mongobee;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Profile({"mongo", "fongo"})
public class MigrationConfig {

    @Bean
    public Mongobee mongobee(MongoClient mongoClient, MongoTemplate mongoTemplate, Environment env) {
        Mongobee mongobee = new Mongobee(mongoClient);
        mongobee.setDbName(env.getProperty("mongo.databaseName"));
        mongobee.setMongoTemplate(mongoTemplate);
        // package to scan for migrations
        mongobee.setChangeLogsScanPackage("de.adorsys.multibanking.dbmigrations");
        mongobee.setEnabled(true);
        return mongobee;
    }

}
