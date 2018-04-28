package de.adorsys.multibanking.service.base;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.adorsys.cryptoutils.exceptions.BaseException;
import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.service.types.DocumentContent;

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
public abstract class CacheBasedService extends DocumentBasedService {

	private ObjectMapper objectMapper;
	public CacheBasedService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	protected abstract UserContext user();
	
	public UserContextCache userContextCache() {
		return new UserContextCache(user());
	}

	/**
	 * Load file from location documentFQN and parse using valueType. Check and
	 * return from cache is available. Caches result it not yet done.
	 *
	 * @param documentFQN
	 * @param valueType
	 * @return
	 */
	public <T> Optional<T> load(DocumentFQN documentFQN, TypeReference<T> valueType) {
		// Log request count
		user().getRequestCounter().load(documentFQN);
		
		// Check cache.
		Optional<CacheEntry<T>> cacheHit = userContextCache().cacheHit(documentFQN, valueType);
		if (cacheHit.isPresent()) {
			user().getRequestCounter().cacheHit(documentFQN);
			return cacheHit.get().getEntry();
		}
		
		// Return empty if base document does not exist.
		if (!documentExists(documentFQN)) return Optional.empty(); 

		try {
			Optional<T> ot = Optional.of(objectMapper.readValue(loadDocument(documentFQN).getDocumentContent().getValue(), valueType));
			
			// Cache document.
			userContextCache().cacheHit(documentFQN, valueType, ot, false);
			return ot;
		} catch (IOException e) {
			throw new BaseException(e);
		}
	}
	
	/**
	 * Remove all Objects whose path start with the corresponding path.
	 * 
	 * @param accessId
	 */
	public void clearCached(DocumentDirectoryFQN dir) {
		userContextCache().clearCached(dir);
		
	}

	/**
	 * Check existence of a document in the storage. Uses the valueType to locate 
	 * and check existence of a cached version.
	 * 
	 * @param documentFQN
	 * @param valueType
	 * @return
	 */
	public <T> boolean documentExists(DocumentFQN documentFQN, TypeReference<T> valueType) {
		if (userContextCache().isCached(documentFQN, valueType))return true;
		return documentExists(documentFQN);
	}

	/**
	 * Store the file in cache. If cache not supported, flush document.
	 *
	 * @param documentFQN
	 * @param valueType
	 * @param entity
	 */
	public <T> void store(DocumentFQN documentFQN, TypeReference<T> valueType, T entity) {
		user().getRequestCounter().store(documentFQN);
		boolean cacheHit = userContextCache().cacheHit(documentFQN, valueType, Optional.ofNullable(entity), true);
		if (!cacheHit)flush(documentFQN, entity);
	}

	public ResourceNotFoundException resourceNotFound(Class<?> klass, String id) {
		return new ResourceNotFoundException(klass, id);
	}
	
	protected <T> void flush(DocumentFQN documentFQN, T entity) {
		user().getRequestCounter().flush(documentFQN);
		DocumentContent documentContent;
		try {
			documentContent = new DocumentContent(objectMapper.writeValueAsBytes(entity));
		} catch (JsonProcessingException e) {
			throw new BaseException(e);
		}
		DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
		storeDocument(dsDocument);
	}

	public void enableCaching() {
		user().setCacheEnabled(true);
	}

	public void flush() {
		if (!user().isCacheEnabled())return;
		Collection<Map<DocumentFQN, CacheEntry<?>>> values = user().getCache().values();
		for (Map<DocumentFQN, CacheEntry<?>> map : values) {
			Collection<CacheEntry<?>> collection = map.values();
			for (CacheEntry<?> cacheEntry : collection) {
				if (cacheEntry.isDirty()) {
					if (cacheEntry.getEntry().isPresent()) {
						flush(cacheEntry.getDocFqn(), cacheEntry.getEntry().get());
					} else {
						deleteDocument(cacheEntry.getDocFqn());
					}
				}
			}
		}
	}

}
