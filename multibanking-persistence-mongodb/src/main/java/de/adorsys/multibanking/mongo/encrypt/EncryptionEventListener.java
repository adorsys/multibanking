package de.adorsys.multibanking.mongo.encrypt;

import de.adorsys.multibanking.domain.UserSecret;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile({"mongo", "fongo"})
@RequiredArgsConstructor
@Component
public class EncryptionEventListener extends AbstractMongoEventListener<Object> {

    private static final String ENCRYPTION_METHOD = "AES";
    private static final String ENCRYPTION_FIELD = "encrypted";
    private static final String ID_FIELD = "_id";
    private final UserSecret userSecret;
    @Value("${db_secret}")
    private String databaseSecret;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        Object source = event.getSource();

        if (!source.getClass().isAnnotationPresent(Encrypted.class)) {
            return;
        }

        Document srcDocument = event.getDocument();
        List<List<String>> excludes = loadExcludes((source.getClass().getAnnotation(Encrypted.class)).exclude());

        //collect excluded fields
        Document documentWithExcludes = documentWithExcludes(srcDocument, excludes);

        //remove excluded fields form source document
        removeExcludes(srcDocument, excludes);

        //collect field for encrption
        String fieldsForEncrypt = srcDocument.toJson();

        //clean the source document
        removeAllFields(srcDocument);

        //put excluded and encrypted fields to source document
        srcDocument.putAll(documentWithExcludes);
        srcDocument.put(ENCRYPTION_FIELD, EncryptionUtil.encrypt(fieldsForEncrypt, secretKey()));
    }

    @Override
    public void onAfterLoad(AfterLoadEvent event) {
        Class source = event.getType();

        if (!source.isAnnotationPresent(Encrypted.class) || event.getDocument().get(ENCRYPTION_FIELD) == null) {
            return;
        }

        Document document = event.getDocument();
        String decryptedJson = EncryptionUtil.decrypt(document.get(ENCRYPTION_FIELD).toString(), secretKey());

        Document decryptedDocument = Document.parse(decryptedJson);

        decryptedDocument.forEach((key, value) -> {
            if (!key.equals(ID_FIELD)) {
                document.put(key, value);
            }
        });
    }

    private List<List<String>> loadExcludes(String[] excludes) {
        return Stream.of(excludes)
                .map(exclude -> Arrays.asList(StringUtils.splitByWholeSeparator(exclude, ".")))
                .collect(Collectors.toList());
    }

    private void removeExcludes(Document document, List<List<String>> excludes) {
        if (excludes.isEmpty()) {
            return;
        }

        excludes.forEach(exclude -> {
            if (!exclude.isEmpty()) {
                document.remove(exclude.get(0));
            }

        });
    }

    private void removeAllFields(Document document) {
        String[] fields = document.keySet().toArray(new String[0]);

        for (String field : fields) {
            document.remove(field);
        }
    }

    private Document documentWithExcludes(Document document, List<List<String>> excludes) {
        Document copy = new Document();
        excludes.forEach(exclude -> copyField(exclude, document, copy));
        return copy;
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
            copyField(excludes.subList(1, excludes.size()), (Document) obj, (Document) copy.get(field));
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
