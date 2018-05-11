package de.adorsys.mbs.service.example.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.adorsys.multibanking.analytics.connector.CreditorIdValidator;
import de.adorsys.multibanking.analytics.connector.DECreditorIdValidator;
import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.Rule;
import de.adorsys.smartanalytics.api.SmartAnalyticsFacade;
import de.adorsys.smartanalytics.api.WrappedBooking;

/**
 * This is a simple embedded smart analytic connector for test purpose.
 * 
 * @author fpo 2018-05-06 06:02
 *
 */
public class SimpleEmbededSmartAnalyticsFacade implements SmartAnalyticsFacade {
	
	Map<String, Rule> creditorIdRules = new HashMap<>();
	Map<String, Rule> otherIbanRule = new HashMap<>();
	
	CreditorIdValidator validator = new DECreditorIdValidator();	

	@Override
	public AnalyticsResult analyzeBookings(AnalyticsRequest analyticsRequest) {
		List<Booking> bookings = analyticsRequest.getBookings();
		List<WrappedBooking> categorized = new ArrayList<>();
		bookings.forEach(booking -> {
			WrappedBooking wrappedBooking = new WrappedBooking(booking);
			String creditorId = booking.getCreditorId();
			if(!validator.isValid(creditorId)){
				creditorId = validator.find(booking.getPurpose());
				if(creditorId!=null){
					String nationalId = validator.nationalId(creditorId);
					Rule rule = creditorIdRules.get(nationalId);
					wrappedBooking.setMainCategory(rule.getMainCategory());
					wrappedBooking.setSubCategory(rule.getSubCategory());
					wrappedBooking.setSpecification(rule.getSpecification());
//					wrappedBooking.setReceiver(rule.getReceiver());
					wrappedBooking.setLogo(rule.getLogo());
					wrappedBooking.setHomepage(rule.getHomepage());
					wrappedBooking.setHotline(rule.getHotline());
					wrappedBooking.setEmail(rule.getEmail());
					if(wrappedBooking.getBooking().getCreditorId()==null){
						wrappedBooking.getBooking().setCreditorId(rule.getCreditorId());
					}
				}
				categorized.add(wrappedBooking);
			}
		});
		AnalyticsResult analyticResult = new AnalyticsResult();
		analyticResult.setBookings(categorized);
		return analyticResult;
	}
}
