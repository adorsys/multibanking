package de.adorsys.multibanking.service.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.service.base.UserObjectService;

@Service
public class UserCacheService {
	@Autowired
	private UserObjectService uso;
	
	public void preHandle(){
		uso.enableCaching();
	}
	
	public void postHandle(){
		uso.flush();
	}
}
