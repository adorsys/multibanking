package de.adorsys.multibanking.encrypt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by alexg on 09.05.17.
 */
@Component
public class EncryptionEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    Principal principal;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SecretKey secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (source.getClass().isAnnotationPresent(Encrypted.class)) {
            try {
                //encrypt dbobject
                String json = objectMapper.writeValueAsString(source);
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


}
