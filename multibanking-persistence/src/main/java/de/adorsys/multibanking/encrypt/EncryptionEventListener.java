package de.adorsys.multibanking.encrypt;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alexg on 09.05.17.
 */
@Component
public class EncryptionEventListener extends AbstractMongoEventListener<Object> {

    private static final String ENCRYPTION_METHOD = "AES";
    private static final String ID_FIELD = "_id";
    private static final String ENCRYPTION_FIELD = "encrypted";

    @Value("${db_secret}")
    private String databaseSecret;
    @Autowired
    private UserSecret userSecret;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (!source.getClass().isAnnotationPresent(Encrypted.class)) {
            return;
        }

        //encrypt dbobject
        DBObject dbObject = event.getDBObject();
        String json = JSON.serialize(dbObject);
        String encrypted = EncryptionUtil.encrypt(json, secretKey());
        List<List<String>> excludes = loadExcludes((source.getClass().getAnnotation(Encrypted.class)).exclude());

        mergeFields(excludes, dbObject);
        dbObject.put(ENCRYPTION_FIELD, encrypted);
    }

    @Override
    public void onAfterLoad(AfterLoadEvent event) {
        Class source = event.getType();

        if (!source.isAnnotationPresent(Encrypted.class) || event.getDBObject().get(ENCRYPTION_FIELD) == null) {
            return;
        }

        DBObject dbObject = event.getDBObject();
        String decryptedJson = EncryptionUtil.decrypt(dbObject.get(ENCRYPTION_FIELD).toString(), secretKey());

        List<List<String>> excludes = loadExcludes(((Encrypted) source.getAnnotation(Encrypted.class)).exclude());

        DBObject decryptedDbObject = (DBObject) JSON.parse(decryptedJson);

        mergeFields(excludes, dbObject, decryptedDbObject);
    }

    private List<List<String>> loadExcludes(String[] excludes) {
        return Stream.of(excludes)
                .map(exclude -> Arrays.asList(StringUtils.splitByWholeSeparator(exclude, ".")))
                .collect(Collectors.toList());
    }

    private void removeAllFields(DBObject dbObject) {
        String[] fields = dbObject.keySet().toArray(new String[0]);

        for (String field : fields) {
//            if (!ID_FIELD.equals(field)) {
            dbObject.removeField(field);
//            }
        }
    }

    private void mergeFields(List<List<String>> excludes, DBObject dbObject) {
        BasicDBObject copy = new BasicDBObject();
        excludes.forEach(exclude -> copyField(exclude, dbObject, copy));
        removeAllFields(dbObject);
        //convertMapToDBObject(copy);
        dbObject.putAll(copy.toMap());
    }

    private void mergeFields(List<List<String>> excludes, DBObject dbObject, DBObject decryptedDbObject) {
        DBObject copy = createDbObject(dbObject, decryptedDbObject);
        //excludes.forEach(exclude -> copyField(exclude, dbObject, copy));
        removeAllFields(dbObject);
        convertMapToDBObject(copy);
        dbObject.putAll(copy.toMap());
    }

    private DBObject createDbObject(DBObject dbObject, DBObject decryptedDbObject) {
        BasicDBObject copy = decryptedDbObject == null
                ? new BasicDBObject()
                : new BasicDBObject(decryptedDbObject.toMap());

        if (copy.get(ID_FIELD) == null) {
            copy.put(ID_FIELD, dbObject.get(ID_FIELD));
        }

        return copy;
    }

    private void convertMapToDBObject(DBObject dbObject) {

        String[] fields = dbObject.keySet().toArray(new String[0]);

        for (String field : fields) {
            Object obj = dbObject.get(field);

            if (obj instanceof Map) {
                BasicDBObject basicDBObject = new BasicDBObject((Map) obj);

                dbObject.put(field, basicDBObject);

                convertMapToDBObject(basicDBObject);
            }
        }
    }

    private void copyField(List<String> excludes, DBObject dbObject, BasicDBObject basicDBObject) {

        if (excludes.isEmpty()) {
            return;
        }

        String field = excludes.get(0);
        Object obj = dbObject.get(field);

        basicDBObject.put(field, obj);

        if (obj instanceof DBObject) {
            copyField(excludes.subList(1, excludes.size()), (DBObject)obj, (BasicDBObject) basicDBObject.get(field));
        }
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(getUserSecret().getBytes(), ENCRYPTION_METHOD);
    }

    private String getUserSecret() {
        try {
            if (userSecret.getSecret() == null) {
                return databaseSecret;
            }
            return userSecret.getSecret();
            //user secret not available outside request scopes
        } catch (BeanCreationException e) {
            return databaseSecret;
        }
    }
}
