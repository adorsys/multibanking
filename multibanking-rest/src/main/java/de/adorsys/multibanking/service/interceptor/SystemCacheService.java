package de.adorsys.multibanking.service.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.service.base.SystemObjectService;

@Service
public class SystemCacheService {
	
	@Autowired
	private SystemObjectService sos;
	
	public void preHandle(){
		sos.enableCaching();
	}
	
	public void postHandle(){
		sos.flush();
	}
}
