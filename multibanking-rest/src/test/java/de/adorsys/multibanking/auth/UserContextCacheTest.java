package de.adorsys.multibanking.auth;

import java.util.Optional;

import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;
import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

public class UserContextCacheTest {

	@Test
	public void cacheHit_stores_and_returns_true_on_cache_enabled() {
		UserContext userContext = enabledUserContext();
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN("/dir/cachedObject.aes");
		CachedObject cachedObject = new CachedObject();
		boolean cacheHit = cache.cacheHit(documentFQN, cachedObjectTypeRef, Optional.of(cachedObject), true);
		Assert.assertTrue(cacheHit);
	}

	@Test
	public void cacheHit_returns_false_on_cache_disabled() {
		UserContext userContext = new UserContext();
		userContext.setCacheEnabled(false);
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN("/dir/cachedObject.aes");
		CachedObject cachedObject = new CachedObject();
		boolean cacheHit = cache.cacheHit(documentFQN, cachedObjectTypeRef, Optional.of(cachedObject), true);
		Assert.assertFalse(cacheHit);
	}

	@Test
	public void isCached_returns_true_if_object_in_cache() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");		
		Assert.assertTrue(isCacheObjectInCache(userContext, "/dir/cachedObject.aes"));
	}

	@Test
	public void isCached_returns_false_if_object_in_cache_but_false_fqn() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");		
		DocumentFQN documentFQN2 = new DocumentFQN("/dir/cachedObject2.aes");
		boolean cached = new UserContextCache(userContext).isCached(documentFQN2, cachedObjectTypeRef);
		Assert.assertFalse(cached);
	}

	@Test
	public void isCached_returns_false_if_object_in_cache_but_false_type() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");		
		DocumentFQN documentFQN2 = new DocumentFQN("/dir/cachedObject2.aes");
		boolean cached = new UserContextCache(userContext).isCached(documentFQN2, simpleObjectTypeRef);
		Assert.assertFalse(cached);
	}
	
	@Test
	public void cacheHit_returns_object_if_in_cache() {
		UserContext userContext = enabledUserContext();
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN("/dir/cachedObject.aes");
		CachedObject cachedObject = new CachedObject();
		boolean cacheHit = cache.cacheHit(documentFQN, cachedObjectTypeRef, Optional.of(cachedObject), true);
		Assume.assumeTrue(cacheHit);
		Optional<CacheEntry<CachedObject>> cacheResult = cache.cacheHit(documentFQN, cachedObjectTypeRef);
		Assert.assertTrue(cacheResult.isPresent());
		Optional<CachedObject> cachedObjectResult = cacheResult.get().getEntry();
		Assert.assertTrue(cachedObjectResult.isPresent());
		Assert.assertTrue(cachedObject==cachedObjectResult.get());
	}

	@Test
	public void cacheHit_returns_empty_if_object_in_cache_but_false_fqn() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");		
		DocumentFQN documentFQN2 = new DocumentFQN("/dir/cachedObject2.aes");
		Optional<CacheEntry<CachedObject>> cacheResult = new UserContextCache(userContext).cacheHit(documentFQN2, cachedObjectTypeRef);
		Assert.assertFalse(cacheResult.isPresent());
	}
	
	@Test
	public void cacheHit_returns_empty_if_object_in_cache_but_false_type() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");
		cacheObjectCache(userContext, "/dir/cachedObject2.aes");
		DocumentFQN documentFQN2 = new DocumentFQN("/dir/cachedObject2.aes");
		UserContextCache cache = new UserContextCache(userContext);
		Optional<CacheEntry<SimpleObject>> cacheResult = cache.cacheHit(documentFQN2, simpleObjectTypeRef);
		Assert.assertFalse(cacheResult.isPresent());
	}

	@Test
	public void clearCached_remove_only_object_from_subpath() {
		UserContext userContext = enabledUserContext();
		cacheObjectCache(userContext, "/dir/cachedObject.aes");
		cacheObjectCache(userContext, "/dir/cachedObject2.aes");
		cacheObjectCache(userContext, "/dir/subdir/cachedObject.aes");
		cacheObjectCache(userContext, "/dir/subdir/cachedObject2.aes");
		cacheObjectCache(userContext, "/dir/subdir/subdir2/cachedObject.aes");
		cacheObjectCache(userContext, "/dir/subdir/subdir2/cachedObject2.aes");
		cacheObjectCache(userContext, "cachedObject.aes");
		UserContextCache cache = new UserContextCache(userContext);
		cache.clearCached(new DocumentDirectoryFQN("/dir/subdir"));
		Assert.assertTrue(isCacheObjectInCache(userContext, "/dir/cachedObject.aes"));
		Assert.assertTrue(isCacheObjectInCache(userContext, "/dir/cachedObject2.aes"));
		Assert.assertTrue(isCacheObjectInCache(userContext, "cachedObject.aes"));

		Assert.assertTrue(isNotCacheObjectInCacheOrIsDirty(userContext, "/dir/subdir/cachedObject.aes"));
		Assert.assertTrue(isNotCacheObjectInCacheOrIsDirty(userContext, "/dir/subdir/cachedObject2.aes"));
		Assert.assertTrue(isNotCacheObjectInCacheOrIsDirty(userContext, "/dir/subdir/subdir2/cachedObject.aes"));
		Assert.assertTrue(isNotCacheObjectInCacheOrIsDirty(userContext, "/dir/subdir/subdir2/cachedObject2.aes"));
	}
	

	private static UserContext enabledUserContext(){
		UserContext userContext = new UserContext();
		userContext.setCacheEnabled(true);
		return userContext;
	}
	private static CachedObject cacheObjectCache(UserContext userContext, String path){
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN(path);
		CachedObject cachedObject = new CachedObject();
		boolean cacheHit = cache.cacheHit(documentFQN, cachedObjectTypeRef, Optional.of(cachedObject), true);
		Assume.assumeTrue(cacheHit);
		return cachedObject;
	}
	private static boolean isCacheObjectInCache(UserContext userContext, String path){
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN(path);
		return cache.isCached(documentFQN, cachedObjectTypeRef);
	}
	private boolean isNotCacheObjectInCacheOrIsDirty(UserContext userContext, String path) {
		UserContextCache cache = new UserContextCache(userContext);
		DocumentFQN documentFQN = new DocumentFQN(path);
		return !cache.isCached(documentFQN, cachedObjectTypeRef) || cache.isDirty(documentFQN, cachedObjectTypeRef);
	}
	
	static final TypeReference<CachedObject> cachedObjectTypeRef = new TypeReference<CachedObject>() {};
	static class CachedObject{}

	static final TypeReference<SimpleObject> simpleObjectTypeRef = new TypeReference<SimpleObject>() {};
	static class SimpleObject{}
	
}
