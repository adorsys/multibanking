package de.adorsys.multibanking.analytics;

/**
 * The {@link DomainAnalyticsService} is the interface that allows us to trigger the analytics
 * process provided by the consumer of the multibanking application.
 * 
 * In order to allow for highest level of decoupling, this module does not presume
 * any specific data model.
 * 
 * We assume that the connected analytics service will have to stream though bookings
 * of the connected account and process them in any order required by the domain business
 * logic to gain valuable information for that consumer.
 * 
 * @author fpo 2018-03-17 09:31
 */
public interface DomainAnalyticsService {
	
	/**
	 * Starts the account analytics process.
	 * 
	 * @param bankAccessId
	 * @param bankAccountId
	 */
    public void startAccountAnalytics(String bankAccessId, String bankAccountId);

}
