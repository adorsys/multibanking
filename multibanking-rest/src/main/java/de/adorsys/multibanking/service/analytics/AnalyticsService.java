package de.adorsys.multibanking.service.analytics;

import org.adorsys.docusafe.business.types.complex.DSDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.analytics.DomainAnalyticsService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.utils.FQNUtils;


/**
 * For each account
 * - Triggers category processing
 * - Creates account analytics
 * - Identify and store contracts.
 * 
 * The analytics process is generally very specific to the business logic of the user
 * of this modules. Consumer of this module will then have to provide a corresponding implementation
 * of this interface in form of a bean.
 * 
 * @author fpo 2018-03-17 09:31
 */
@Service
public class AnalyticsService {
	
	@Autowired
	private DomainAnalyticsService domainAnalyticsService;
	
	@Autowired
	private UserObjectService uos;
	
	/**
	 * Starts the account analytics process
	 * 
	 * @param bankAccount
	 */
    public void startAccountAnalytics(String accessId, String accountId){
    	domainAnalyticsService.startAccountAnalytics(accessId, accountId);
    }
    
    /**
     * Loads and returns the account analytics file to the user.
     * 
     * We make no assumption on the structure of the domain analytics object. This is returned to
     * the user as specified by the domain business model.
     * 
     * @param bankAccount
     * @return
     */
    public DSDocument loadDomainAnalytics(String accessId, String accountId){
    	return uos.loadDocument(FQNUtils.analyticsFQN(accessId, accountId));
    }
}
