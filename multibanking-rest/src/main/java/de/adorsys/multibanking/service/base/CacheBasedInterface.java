package de.adorsys.multibanking.service.base;

import com.fasterxml.jackson.core.type.TypeReference;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;

import java.util.Optional;

/**
 * Created by peter on 11.06.18 at 10:35.
 */
public interface CacheBasedInterface {
    <T> Optional<T> load(DocumentFQN documentFQN, TypeReference<T> valueType);

    <T> void store(DocumentFQN documentFQN, TypeReference<T> valueType, T entity);

    <T> boolean documentExists(DocumentFQN documentFQN, TypeReference<T> valueType);

    <T> boolean deleteDocument(DocumentFQN documentFQN, TypeReference<T> valueType);

    public void deleteDirectory(DocumentDirectoryFQN dirFQN);

    public void flush();
}
