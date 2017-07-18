package de.adorsys.multibanking.conf;

import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexg on 08.02.17.
 */
@Configuration
@PropertySource(value = "${mongo.properties.url}", ignoreResourceNotFound = true)
@Profile({"mongo"})
public class MongoConfig extends AbstractMongoConfiguration {

    @Autowired
    Environment env;

    @Override
    protected String getDatabaseName() {
        return env.getProperty("mongo.databaseName");
    }

    @Override
    public Mongo mongo() throws Exception {
        return mongoClient();
    }

    @Bean
    @Autowired
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        return new ContinueOnBatchErrorTemplate(mongoDbFactory());
    }

    @Bean
    public MongoClient mongoClient() throws UnknownHostException {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.connectionsPerHost(50);
        builder.writeConcern(WriteConcern.JOURNALED);
        builder.readPreference(ReadPreference.secondaryPreferred());
        MongoClientOptions options = builder.build();

        ServerAddress serverAddress = getServerAddress();

        if (StringUtils.isEmpty(env.getProperty("mongo.userName"))) {
            return new MongoClient(serverAddress, options);
        } else {
            MongoCredential mongoCredential = MongoCredential
                    .createCredential(env.getProperty("mongo.userName"), env.getProperty("mongo.databaseName"),
                            env.getProperty("mongo.password").toCharArray());

            return new MongoClient(serverAddress, Collections.singletonList(mongoCredential), options);
        }
    }

    private ServerAddress getServerAddress() {
        String[] serverParts = env.getProperty("mongo.server").replace("mongodb://", "").split(":");
        return new ServerAddress(serverParts[0],
                1 < serverParts.length ? Integer.valueOf(serverParts[1]) : ServerAddress.defaultPort());
    }

    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        return new SimpleMongoDbFactory(mongoClient(), env.getProperty("mongo.databaseName"));
    }

    private class ContinueOnBatchErrorTemplate extends MongoTemplate {

        private static final String ID_FIELD = "_id";

        public ContinueOnBatchErrorTemplate(MongoDbFactory mongoDbFactory) {
            super(mongoDbFactory);
        }

        protected List<ObjectId> insertDBObjectList(final String collectionName, final List<DBObject> dbDocList) {
            if (dbDocList.isEmpty()) {
                return Collections.emptyList();
            }

            execute(collectionName, new CollectionCallback<Void>() {
                public Void doInCollection(DBCollection collection) throws MongoException, DataAccessException {
                    MongoAction mongoAction = new MongoAction(null, MongoActionOperation.INSERT_LIST, collectionName, null,
                            null, null);
                    WriteConcern writeConcernToUse = prepareWriteConcern(mongoAction);
                    //TODO remove this bullshit when springframework data supports InsertOptions
                    InsertOptions insertOptions = (new InsertOptions()).writeConcern(writeConcernToUse).continueOnError(collectionName.equals("booking"));

                    WriteResult writeResult = collection.insert(dbDocList, insertOptions);
                    handleAnyWriteResultErrors(writeResult, null, MongoActionOperation.INSERT_LIST);
                    return null;
                }
            });

            List<ObjectId> ids = new ArrayList<>();
            for (DBObject dbo : dbDocList) {
                Object id = dbo.get(ID_FIELD);
                if (id instanceof ObjectId) {
                    ids.add((ObjectId) id);
                } else {
                    // no id was generated
                    ids.add(null);
                }
            }
            return ids;
        }
    }
}


