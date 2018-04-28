package de.adorsys.multibanking.auth;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;

import de.adorsys.sts.tokenauth.BearerToken;
import lombok.Data;

/**
 * Java class to store contextual information associated with the user.
 * 
 * @author fpo
 *
 */
@Data
public class UserContext {
	private BearerToken bearerToken;
	private UserIDAuth auth;
	private RequestCounter requestCounter = new RequestCounter();

	Map<Type, Map<DocumentFQN, CacheEntry<?>>> cache = new HashMap<>();
	
	boolean cacheEnabled = false;
}
