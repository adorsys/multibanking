package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.DocumentSafeService;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;

/**
 * Service that access the system repository use this service.
 * 
 * @author fpo 2018-04-06 06:00
 *
 */
public class SystemObjectService extends CacheBasedService {
	private SystemContext systemContext;

	public SystemObjectService(ObjectMapper objectMapper, SystemContext systemContext, DocumentSafeService documentSafeService) {
	    super(objectMapper, documentSafeService);
		this.systemContext = systemContext;
	}

	@Override
	public UserContext user() {
		return systemContext.getUser();
	}
}
