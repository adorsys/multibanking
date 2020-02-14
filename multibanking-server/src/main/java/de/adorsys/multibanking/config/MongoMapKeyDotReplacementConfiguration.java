package de.adorsys.multibanking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Profile({"mongo", "fongo"})
@Configuration
public class MongoMapKeyDotReplacementConfiguration {

    @Autowired
    public void setMapKeyDotReplacement(MappingMongoConverter mongoConverter) {
        mongoConverter.setMapKeyDotReplacement("#");
    }
}
