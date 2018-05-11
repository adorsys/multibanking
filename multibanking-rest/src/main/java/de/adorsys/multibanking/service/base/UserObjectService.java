package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.types.complex.UserIDAuth;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.auth.UserContext;

/**
 * Services that access the repository of the current user use this service.
 * 
 * @author fpo 2018-04-06 05:00
 *
 */
public class UserObjectService extends CacheBasedService {
	private UserContext userContext;

	public UserObjectService(ObjectMapper objectMapper, UserContext userContext) {
		super(objectMapper);
		this.userContext = userContext;
	}

	@Override
	public UserContext user() {
		return userContext;
	}

	@Override
	public UserIDAuth auth() {
		return userContext.getAuth();
	}
}
