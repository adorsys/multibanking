package de.adorsys.multibanking.service.base;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.service.types.DocumentContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.auth.CacheEntry;
import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.auth.UserContextCache;
import de.adorsys.multibanking.exception.ResourceNotFoundException;

/**
 * Base class for providing access to object thru cache.
 *
 * Provides caching functionality when enabled.
 *
 * @author fpo 2018-04-06 04:36
 *
 */
public abstract class CacheBasedService implements CacheBasedInterface {
    private final static Logger LOGGER = LoggerFactory.getLogger(CacheBasedService.class);

	private ObjectMapper objectMapper;
	private DocumentSafeService documentSafeService;
	protected abstract UserContext user();

	public UserContextCache userContextCache() {
		return new UserContextCache(user());
	}

	public void enableCaching() {
		user().setCacheEnabled(true);
	}

	protected CacheBasedService(ObjectMapper objectMapper, DocumentSafeService documentSafeService) {
		this.objectMapper = objectMapper;
		this.documentSafeService = documentSafeService;
	}

	public UserIDAuth auth() {
		return user().getAuth();
	}

	/**
	 * Load file from location documentFQN and parse using valueType. Check and
	 * return from cache is available. Caches result it not yet done.
	 *
	 * @param documentFQN
	 * @param valueType
	 * @return
	 */
	@Override
	public <T> Optional<T> load(DocumentFQN documentFQN, TypeReference<T> valueType) {
        LOGGER.debug("load: " + documentFQN);
		// Log request count
		user().getRequestCounter().load(documentFQN);

		// Check cache.
		Optional<CacheEntry<T>> cacheHit = userContextCache().cacheHit(documentFQN, valueType);
		if (cacheHit.isPresent()) {
	        LOGGER.debug("loaded from cache: " + documentFQN);
			user().getRequestCounter().cacheHit(documentFQN);
			return cacheHit.get().getEntry();
		}

		// Return empty if base document does not exist.
		if (!documentSafeService.documentExists(user().getAuth(), documentFQN)){
	        LOGGER.debug("load, doc not found: " + documentFQN);
			return Optional.empty();
		}

		try {
	        LOGGER.debug("loading from file: " + documentFQN);
			Optional<T> ot = Optional.of(objectMapper.readValue(documentSafeService.readDocument(user().getAuth(), documentFQN).getDocumentContent().getValue(), valueType));

			// Cache document.
			userContextCache().cacheHit(documentFQN, valueType, ot, false);
			return ot;
		} catch (IOException e) {
			throw new BaseException(e);
		}
	}

	/**
	 * Store the file in cache. If cache not supported, flush document.
	 *
	 * @param documentFQN
	 * @param valueType
	 * @param entity
	 */
	@Override
	public <T> void store(DocumentFQN documentFQN, TypeReference<T> valueType, T entity) {
		LOGGER.debug("store: " + documentFQN + " cache enabled:" + user().isCacheEnabled());
		user().getRequestCounter().store(documentFQN);
		boolean cacheHit = userContextCache().cacheHit(documentFQN, valueType, Optional.ofNullable(entity), true);
		if (!cacheHit) {
			LOGGER.debug("flush im store " + documentFQN);
			flush(documentFQN, entity);
		} else {
			LOGGER.debug("No flush, will store on cache flush " + documentFQN);
		}
	}

	/**
	 * Check existence of a document in the storage. Uses the valueType to locate
	 * and check existence of a cached version.
	 *
	 * @param documentFQN
	 * @param valueType
	 * @return
	 */
	@Override
	public <T> boolean documentExists(DocumentFQN documentFQN, TypeReference<T> valueType) {
		if (userContextCache().isCached(documentFQN, valueType))return true;
		return documentSafeService.documentExists(auth(), documentFQN);
	}

	@Override
    public <T> boolean deleteDocument(DocumentFQN documentFQN, TypeReference<T> valueType) {
        LOGGER.debug("deleteDocument " + documentFQN);
        
        // Remove from cache
        Optional<CacheEntry<T>> removed = userContextCache().remove(documentFQN, valueType);
        boolean docExist=false;
        try {
            docExist = documentSafeService.documentExists(auth(), documentFQN);
        } catch (BaseException b){
            LOGGER.warn("error checking existence of Document " + documentFQN);
            // No Action. might nit have been flushed yet.
        }
        if(docExist){
            documentSafeService.deleteDocument(auth(), documentFQN);
            return true;
        }
        return removed!=null;
    }

	@Override
	public void deleteDirectory(DocumentDirectoryFQN dirFQN) {
		// First remove all cached object from this dir.
		clearCached(dirFQN);
		documentSafeService.deleteFolder(auth(), dirFQN);
	}

	@Override
	public void flush() {
		if (!user().isCacheEnabled())return;
		Collection<Map<DocumentFQN, CacheEntry<?>>> values = user().getCache().values();
		LOGGER.debug("Flushing cache: " + user().getAuth().getUserID() + " Objects in cache: " + values.size());
		for (Map<DocumentFQN, CacheEntry<?>> map : values) {
			Collection<CacheEntry<?>> collection = map.values();
			for (CacheEntry<?> cacheEntry : collection) {
				LOGGER.debug("Cache entry pre flush: " + cacheEntry.getDocFqn());
				if (cacheEntry.isDirty()) {
					cacheEntry.setDirty(false);
					LOGGER.debug("Cache entry pre flush : dirty: " + cacheEntry.getDocFqn());
					if (cacheEntry.getEntry().isPresent()) {
						LOGGER.debug("Cache entry pre flush : present: " + cacheEntry.getDocFqn());
						flush(cacheEntry.getDocFqn(), cacheEntry.getEntry().get());
					} else {
						LOGGER.debug("Cache entry pre flush : absent. File will be deleted: " + cacheEntry.getDocFqn());
						documentSafeService.deleteDocument(auth(), cacheEntry.getDocFqn());
					}
				} else {
					LOGGER.debug("Cache entry pre flush : clean. No file write : " + cacheEntry.getDocFqn());
				}
			}
		}
		LOGGER.debug("Flushed cache: " + user().getAuth().getUserID());
	}

	/**
	 * Remove all Objects whose path start with the corresponding path.
	 *
	 * @param accessId
	 */
	private void clearCached(DocumentDirectoryFQN dir) {
		LOGGER.debug("clearing Cached " + dir);
		userContextCache().clearCached(dir);

	}

	private <T> void flush(DocumentFQN documentFQN, T entity) {
        LOGGER.debug("flushing: " + documentFQN);

        user().getRequestCounter().flush(documentFQN);
		DocumentContent documentContent;
		try {
			documentContent = new DocumentContent(objectMapper.writeValueAsBytes(entity));
		} catch (JsonProcessingException e) {
			throw new BaseException(e);
		}
		DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
		documentSafeService.storeDocument(auth(), dsDocument);
	}


}
