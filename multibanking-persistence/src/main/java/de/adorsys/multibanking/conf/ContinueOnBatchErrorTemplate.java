package de.adorsys.multibanking.conf;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoAction;
import org.springframework.data.mongodb.core.MongoActionOperation;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by alexg on 01.12.17.
 */
public class ContinueOnBatchErrorTemplate extends MongoTemplate {

    private static List<String> continueOnErrorCollections = Arrays.asList("bookingEntity", "anonymizedBookingEntity");
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
                InsertOptions insertOptions = (new InsertOptions()).writeConcern(writeConcernToUse).continueOnError(continueOnErrorCollections.contains(collectionName));

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
