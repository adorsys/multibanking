package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.DocumentSafeService;

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

	public UserObjectService(ObjectMapper objectMapper, UserContext userContext, DocumentSafeService documentSafeService) {
		super(objectMapper, documentSafeService);
		this.userContext = userContext;
	}

	@Override
	public UserContext user() {
		return userContext;
	}
}
