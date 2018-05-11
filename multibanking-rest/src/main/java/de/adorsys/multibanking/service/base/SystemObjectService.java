package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.types.complex.UserIDAuth;

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

	public SystemObjectService(ObjectMapper objectMapper, SystemContext systemContext) {
		super(objectMapper);
		this.systemContext = systemContext;
	}

	@Override
	public UserContext user() {
		return systemContext.getUser();
	}

	@Override
	public UserIDAuth auth() {
		return systemContext.getUser().getAuth();
	}
}
