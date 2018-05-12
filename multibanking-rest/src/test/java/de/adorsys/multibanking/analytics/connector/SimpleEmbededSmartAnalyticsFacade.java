package de.adorsys.multibanking.analytics.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.multibanking.utils.Ids;
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
	Map<String, Rule> dumyRules = new HashMap<>();
	
	public SimpleEmbededSmartAnalyticsFacade() {
		dumyRules.put("Auszahlung", createRule("Barentnahmen", "Geldautomat", null));
		dumyRules.put("Sparen", createRule("Finanzen", "Sparen & Vorsorge", null));
		dumyRules.put("Lebensmittel", createRule("Lebenshaltung", "Lebensmittel", "Supermarkt"));
		dumyRules.put("Auto", createRule("Verkehr & Mobilität", "Auto", null));
	}
	
	CreditorIdValidator validator = new DECreditorIdValidator();	

	@Override
	public AnalyticsResult analyzeBookings(AnalyticsRequest analyticsRequest) {
		List<Booking> bookings = analyticsRequest.getBookings();
		List<WrappedBooking> categorized = new ArrayList<>();
		bookings.forEach(booking -> {
			WrappedBooking wrappedBooking = new WrappedBooking(booking);
			String creditorId = booking.getCreditorId();
			Rule rule = null;
			if(!validator.isValid(creditorId)){
				creditorId = validator.find(booking.getPurpose());
				if(creditorId!=null){
					String nationalId = validator.nationalId(creditorId);
					rule = creditorIdRules.get(nationalId);
				}
			}
			if(rule==null){
				Entry<String, Rule> ruleEntry = dumyRules.entrySet().stream().filter(re ->
					StringUtils.containsIgnoreCase(booking.getPurpose(), re.getKey())).findFirst().orElse(null);
				rule = ruleEntry!=null?ruleEntry.getValue():null;
			}
			if(rule!=null){
				consumeRule(wrappedBooking, rule);
			}
			categorized.add(wrappedBooking);
		});
		AnalyticsResult analyticResult = new AnalyticsResult();
		analyticResult.setBookings(categorized);
		return analyticResult;
	}
	
	public static void consumeRule(WrappedBooking wrappedBooking, Rule rule){
		wrappedBooking.setMainCategory(rule.getMainCategory());
		wrappedBooking.setSubCategory(rule.getSubCategory());
		wrappedBooking.setSpecification(rule.getSpecification());
//		wrappedBooking.setReceiver(rule.getReceiver());
		wrappedBooking.setLogo(rule.getLogo());
		wrappedBooking.setHomepage(rule.getHomepage());
		wrappedBooking.setHotline(rule.getHotline());
		wrappedBooking.setEmail(rule.getEmail());
		if(wrappedBooking.getBooking().getCreditorId()==null){
			wrappedBooking.getBooking().setCreditorId(rule.getCreditorId());
		}		
	}
	private static Rule createRule(String mainCategory, String spec, String sub){
		Rule rule = new Rule();
		rule.setMainCategory(mainCategory);
		rule.setSpecification(spec);
		rule.setSubCategory(sub);
		rule.setRuleId(Ids.uuid());
		return rule;
	}
}
