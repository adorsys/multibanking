package de.adorsys.multibanking.conf;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

/**
 * Created by alexg on 11.04.17.
 */
@Configuration
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Profile({"fongo"})
public class FongoConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "multibanking";
    }

    @Override
    @Bean
    public Mongo mongo() {
        return new Fongo("multibanking").getMongo();
    }
}
