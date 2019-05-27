package de.adorsys.multibanking.mongo.conf;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoAction;
import org.springframework.data.mongodb.core.MongoActionOperation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.StreamUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ContinueOnBatchErrorTemplate extends MongoTemplate {

    private static final String ID_FIELD = "_id";
    private static List<String> continueOnErrorCollections = Arrays.asList("bookingEntity", "anonymizedBookingEntity");

    public ContinueOnBatchErrorTemplate(MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
    }

    protected List<Object> insertDocumentList(final String collectionName, final List<Document> documents) {

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        execute(collectionName, collection -> {

            MongoAction mongoAction = new MongoAction(null, MongoActionOperation.INSERT_LIST, collectionName, null,
                    null, null);
            WriteConcern writeConcernToUse = prepareWriteConcern(mongoAction);
            //TODO remove this bullshit when springframework data supports InsertOptions
            InsertManyOptions insertOptions =
                    (new InsertManyOptions()).ordered(!continueOnErrorCollections.contains(collectionName));

            if (writeConcernToUse == null) {
                collection.insertMany(documents, insertOptions);
            } else {
                collection.withWriteConcern(writeConcernToUse).insertMany(documents, insertOptions);
            }

            return null;
        });

        return documents.stream()//
                .map(it -> it.get(ID_FIELD))//
                .collect(StreamUtils.toUnmodifiableList());
    }
}
