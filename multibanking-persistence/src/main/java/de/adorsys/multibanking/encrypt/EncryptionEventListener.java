package de.adorsys.multibanking.encrypt;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionEventListener.class);

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (!source.getClass().isAnnotationPresent(Encrypted.class)) {
            return;
        }

        //encrypt dbobject
        Document document = event.getDocument();
        String encrypted = EncryptionUtil.encrypt(document.toJson(), secretKey());
        List<List<String>> excludes = loadExcludes((source.getClass().getAnnotation(Encrypted.class)).exclude());

        mergeFields(excludes, document);
        document.put(ENCRYPTION_FIELD, encrypted);
    }

    @Override
    public void onAfterLoad(AfterLoadEvent event) {
        Class source = event.getType();

        if (!source.isAnnotationPresent(Encrypted.class) || event.getDocument().get(ENCRYPTION_FIELD) == null) {
            return;
        }

        Document document = event.getDocument();
        String decryptedJson = EncryptionUtil.decrypt(document.get(ENCRYPTION_FIELD).toString(), secretKey());

        List<List<String>> excludes = loadExcludes(((Encrypted) source.getAnnotation(Encrypted.class)).exclude());

        Document decryptedDocument = Document.parse(decryptedJson);
        mergeFields(document, decryptedDocument);
    }

    private List<List<String>> loadExcludes(String[] excludes) {
        return Stream.of(excludes)
                .map(exclude -> Arrays.asList(StringUtils.splitByWholeSeparator(exclude, ".")))
                .collect(Collectors.toList());
    }

    private void removeAllFields(Document document) {
        String[] fields = document.keySet().toArray(new String[0]);

        for (String field : fields) {
            document.remove(field);
        }
    }

    private void mergeFields(List<List<String>> excludes, Document document) {
        Document copy = new Document();
        excludes.forEach(exclude -> copyField(exclude, document, copy));
        removeAllFields(document);
        document.putAll(copy);
    }

    private void mergeFields(Document document, Document decryptedDocument) {
        Document copy = createDbObject(document, decryptedDocument);
        removeAllFields(document);
        convertMapToDBObject(copy);
        document.putAll(copy);
    }

    private Document createDbObject(Document document, Document decryptedDocument) {
        Document copy = decryptedDocument == null
                ? new Document()
                : new Document(decryptedDocument);

        if (copy.get(ID_FIELD) == null) {
            copy.put(ID_FIELD, document.get(ID_FIELD));
        }

        return copy;
    }

    private void convertMapToDBObject(Document document) {

        String[] fields = document.keySet().toArray(new String[0]);

        for (String field : fields) {
            Object obj = document.get(field);

            if (obj instanceof Map) {
                Document innerDocument = new Document((Map) obj);

                document.put(field, innerDocument);

                convertMapToDBObject(innerDocument);
            }
        }
    }

    private void copyField(List<String> excludes, Document original, Document copy) {

        if (excludes.isEmpty()) {
            return;
        }

        String field = excludes.get(0);
        Object obj = original.get(field);

        if (obj != null) {
            copy.put(field, obj);
        }


        if (obj instanceof Document) {
            copyField(excludes.subList(1, excludes.size()), (Document)obj, (Document) copy.get(field));
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
