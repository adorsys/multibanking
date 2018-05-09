package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.domain.BookingEntity;
import de.adorsys.multibanking.exception.SmartanalyticsException;
import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.GroupConfig;
import de.adorsys.smartanalytics.api.Rule;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.service.analytics.SmartanalyticsMapper.convertInput;

@Service
public class SmartAnalyticsService {

    @Autowired
    private CustomBookingRuleService bookingRuleService;
    @Autowired
    @Qualifier("smartanalytics")
    private RestTemplate smartanalyticsRestTemplate;
    @Autowired
    @Qualifier("contractBlacklist")
    private List<String> contractBlacklist;
    @Autowired
    private GroupConfig groupConfig;

    public AnalyticsResult analyzeBookings(String userId, List<BookingEntity> bookings) {
        bookings.forEach(bookingEntity -> bookingEntity.setBookingCategory(null));
        List<Rule> customRules = loadUserRules(userId);

        AnalyticsRequest analyticsRequest = createAnalyticsRequest(bookings, customRules);
        return analyzeBookingsRemote(analyticsRequest);
    }

    private AnalyticsResult analyzeBookingsRemote(AnalyticsRequest analyticsRequest) {
        ResponseEntity<Resource<AnalyticsResult>> responseEntity = smartanalyticsRestTemplate
                .exchange(
                        "/api/v1/analytics", HttpMethod.PUT, new HttpEntity<Object>(analyticsRequest),
                        new ParameterizedTypeReference<Resource<AnalyticsResult>>() {
                        }, Collections.emptyMap());

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody().getContent();
        }

        throw new SmartanalyticsException(responseEntity.getStatusCode(), null);
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
