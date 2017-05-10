package de.adorsys.multibanking.encrypt;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;

/**
 * Created by alexg on 09.05.17.
 */
@Component
public class FieldEncryptionEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    Principal principal;

    private SecretKey secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (source.getClass().isAnnotationPresent(Encrypted.class)) {
            Encrypted annotation = event.getSource().getClass().getAnnotation(Encrypted.class);

            for (String field : annotation.fields()) {
                encrypt(event.getDBObject(), field);
            }
        }
    }

    @Override
    public void onAfterLoad(AfterLoadEvent event) {
        Class source = event.getType();

        if (source.isAnnotationPresent(Encrypted.class)) {
            Encrypted annotation = (Encrypted) source.getAnnotation(Encrypted.class);

            Arrays.asList(annotation.fields()).forEach(field -> {
                decrypt(event.getDBObject(), field);
            });
        }
    }

    private void encrypt(DBObject dbObject, String field) {
        String[] fields = StringUtils.split(field, ".");
        Object value = getDBObjectValue(dbObject, fields);

        if (value != null) {
            String encryptedValue = FieldEncryptionUtil.encrypt(value.toString(), secretKey);
            setDBObjectValue(dbObject, fields, encryptedValue);
        }
    }

    private void decrypt(DBObject dbObject, String field) {
        String[] fields = StringUtils.split(field, ".");
        String encrypted = getDBObjectValue(dbObject, fields);

        if (encrypted != null) {
            String decryptedValue = FieldEncryptionUtil.decrypt(encrypted, secretKey);
            setDBObjectValue(dbObject, fields, decryptedValue);
        }
    }

    private void setDBObjectValue(DBObject dbObject, String fields[], String value) {
        for (String field : fields) {
            Object object = dbObject.get(field);
            if (object instanceof BasicDBObject) {
                dbObject = (BasicDBObject)object;
            }
        }
        dbObject.put(fields[fields.length-1], value);
    }

    private String getDBObjectValue(DBObject dbObject, String fields[]) {
        for (String field : fields) {
            Object object = dbObject.get(field);
            if (object == null) {
                return null;
            } else if (object instanceof DBObject) {
                dbObject = (DBObject)object;
            } else {
                return object.toString();
            }
        }
        return null;
    }

}
