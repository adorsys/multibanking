package de.adorsys.multibanking.encrypt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
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

/**
 * Created by alexg on 09.05.17.
 */
@Component
public class EncryptionEventListener extends AbstractMongoEventListener<Object> {

    @Value("${db_secret}")
    String databaseSecret;
    @Autowired
    UserSecret userSecret;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (source.getClass().isAnnotationPresent(Encrypted.class)) {
            try {
                String secret = getUserSecret();
                SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "AES");

                //encrypt dbobject
                String json = objectMapper.writeValueAsString(event.getDBObject());

                String encrypted = EncryptionUtil.encrypt(json, secretKey);

                //cleanup dbobject exclude annotated fields
                List<String> excludeFields = Arrays.asList(source.getClass().getAnnotation(Encrypted.class).exclude());
                Object[] keySet = event.getDBObject().keySet().toArray();
                for (int i = keySet.length - 1; i >= 0; i--) {
                    if (!excludeFields.contains(keySet[i].toString())) {
                        event.getDBObject().removeField(keySet[i].toString());
                    }
                }

                event.getDBObject().put("encrypted", encrypted);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAfterLoad(AfterLoadEvent event) {
        Class source = event.getType();

        if (source.isAnnotationPresent(Encrypted.class)) {
            if (event.getDBObject().get("encrypted") == null) {
                return;
            }

            String secret = getUserSecret();
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            String decryptedJson = EncryptionUtil.decrypt(event.getDBObject().get("encrypted").toString(), secretKey);
            DBObject decryptedDbObject = (DBObject) JSON.parse(decryptedJson);
            List<String> exclude = Arrays.asList(((Encrypted) source.getAnnotation(Encrypted.class)).exclude());

            //cleanup loaded dbobject exclude annotated fields
            Object[] keySet = event.getDBObject().keySet().toArray();
            for (int i = keySet.length - 1; i >= 0; i--) {
                if (!exclude.contains(keySet[i].toString())) {
                    event.getDBObject().removeField(keySet[i].toString());
                }
            }

            //restore encrypted data exclude annotated fields
            keySet = decryptedDbObject.keySet().toArray();
            for (int i = keySet.length - 1; i >= 0; i--) {
                String key = keySet[i].toString();
                if (!exclude.contains(key)) {
                    event.getDBObject().put(key, decryptedDbObject.get(key));
                }
            }
        }
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
