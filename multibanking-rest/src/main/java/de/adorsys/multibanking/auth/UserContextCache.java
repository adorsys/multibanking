package de.adorsys.multibanking.auth;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Java class to store contextual information associated with the user.
 * 
 * @author fpo
 *
 */
public class UserContextCache {
	
	private UserContext userContext;
	public UserContextCache(UserContext userContext) {
		super();
		this.userContext = userContext;
	}

	public <T> Optional<CacheEntry<T>> cacheHit(DocumentFQN documentFQN, TypeReference<T> valueType){
		if(!userContext.isCacheEnabled()) return Optional.empty();
		
		Map<DocumentFQN, CacheEntry<?>> typeCache = typeCache(valueType);
		CacheEntry<T> cacheEntry = (CacheEntry<T>) typeCache.get(documentFQN);
		return Optional.ofNullable(cacheEntry);
	}

	/**
	 * Checks if a document is in the cache.
	 * 
	 * @param documentFQN
	 * @param valueType
	 * @return
	 */
	public <T> boolean isCached(DocumentFQN documentFQN, TypeReference<T> valueType){
		if(!userContext.isCacheEnabled()) return false;
		
		if(valueType==null) return false;
		
		Map<DocumentFQN, CacheEntry<?>> typeCache = typeCache(valueType);
		CacheEntry<T> cacheEntry = (CacheEntry<T>) typeCache.get(documentFQN);
		return cacheEntry!=null;
	}
	
	/**
	 * @param documentFQN
	 * @param valueType
	 * @param entry
	 * @return true if cached. false is caching not supported.
	 */
	public <T> boolean cacheHit(DocumentFQN documentFQN, TypeReference<T> valueType, Optional<T> entry, boolean dirty){
		if(!userContext.isCacheEnabled()) return false;
		Map<DocumentFQN, CacheEntry<?>> typeCache = typeCache(valueType);
		CacheEntry<T> cacheEntry = new CacheEntry<>();
		cacheEntry.setDocFqn(documentFQN);
		cacheEntry.setEntry(entry);
		cacheEntry.setValueType(valueType);
		cacheEntry.setDirty(dirty);
		typeCache.put(documentFQN, cacheEntry);
		return true;
	}
	
	private <T> Map<DocumentFQN, CacheEntry<?>> typeCache(TypeReference<T> valueType) {
		Map<DocumentFQN, CacheEntry<?>> map = userContext.getCache().get(valueType.getType());
		if(map==null){
			map=new HashMap<>();
			userContext.getCache().put(valueType.getType(), map);
		}
		return map;
	}

	public void clearCached(DocumentDirectoryFQN dir) {
		Map<Type, Map<DocumentFQN, CacheEntry<?>>> cache = userContext.getCache();
		Collection<Map<DocumentFQN,CacheEntry<?>>> values = cache.values();
		String path = dir.getValue();
		for (Map<DocumentFQN, CacheEntry<?>> map : values) {
			Set<DocumentFQN> keySet = map.keySet();
			Set<DocumentFQN> keyToRemove = map.keySet();
			for (DocumentFQN documentFQN : keySet) {
				if(StringUtils.startsWith(documentFQN.getValue(), path)){
					keyToRemove.add(documentFQN);
				}
			}
			for (DocumentFQN documentFQN : keyToRemove) {
				map.remove(documentFQN);
			}
		}
	}

}
