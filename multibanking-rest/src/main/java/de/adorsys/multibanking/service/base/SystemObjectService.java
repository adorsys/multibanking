package de.adorsys.multibanking.service.base;

import org.adorsys.docusafe.business.DocumentSafeService;
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
    private DocumentSafeService documentSafeService;

	public SystemObjectService(ObjectMapper objectMapper, SystemContext systemContext, DocumentSafeService documentSafeService) {
		super(objectMapper);
		this.systemContext = systemContext;
		this.documentSafeService = documentSafeService;
	}

	@Override
	public UserContext user() {
		return systemContext.getUser();
	}

	@Override
	public UserIDAuth auth() {
		return systemContext.getUser().getAuth();
	}

    @Override
    protected DocumentSafeService docs() {
        return documentSafeService;
    }
}
