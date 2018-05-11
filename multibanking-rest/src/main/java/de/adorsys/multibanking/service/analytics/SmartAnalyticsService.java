package de.adorsys.multibanking.service.analytics;

import static de.adorsys.multibanking.service.analytics.SmartanalyticsMapper.convertInput;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.GroupConfig;
import de.adorsys.smartanalytics.api.Rule;
import de.adorsys.smartanalytics.api.SmartAnalyticsFacade;

@Service
public class SmartAnalyticsService {

    @Autowired
    private CustomBookingRuleService bookingRuleService;
    @Autowired
    @Qualifier("contractBlacklist")
    private List<String> contractBlacklist;
    @Autowired
    private GroupConfig groupConfig;
    @Autowired
    private SmartAnalyticsFacade smartAnalyticsFacade;

    public AnalyticsResult analyzeBookings(String userId, List<BookingEntity> bookings) {
        bookings.forEach(bookingEntity -> bookingEntity.setBookingCategory(null));
        List<Rule> customRules = loadUserRules(userId);

        AnalyticsRequest analyticsRequest = createAnalyticsRequest(bookings, customRules);
        return analyzeBookingsRemote(analyticsRequest);
    }

    private AnalyticsResult analyzeBookingsRemote(AnalyticsRequest analyticsRequest) {
    	return smartAnalyticsFacade.analyzeBookings(analyticsRequest);
    }

    private List<Rule> loadUserRules(String userId) {
        return bookingRuleService.loadRules().stream()
                .map(customRuleEntity -> {
                    Rule smartanalyticsRule = new Rule();
                    BeanUtils.copyProperties(customRuleEntity, smartanalyticsRule);
                    return smartanalyticsRule;
                })
                .collect(Collectors.toList());
    }

    private AnalyticsRequest createAnalyticsRequest(List<BookingEntity> bookings, List<Rule> customRules) {
        return AnalyticsRequest.builder()
                .bookings(convertInput(bookings))
                .groupConfig(groupConfig)
                .contractBlackListMatcher(contractBlacklist)
                .customRules(customRules)
                .build();
    }
}
