package de.adorsys.multibanking.service.analytics;

import de.adorsys.multibanking.exception.SmartanalyticsException;
import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.Rule;
import de.adorsys.smartanalytics.api.config.ConfigStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Profile("smartanalytics-remote")
public class SmartAnalyticsRemoteService implements SmartAnalyticsIf {

    @Autowired
    @Qualifier("smartanalytics")
    private RestTemplate smartanalyticsRestTemplate;

    public AnalyticsResult analyzeBookings(List<Rule> userRules, List<Booking> bookings) {
        AnalyticsRequest analyticsRequest = createAnalyticsRequest(bookings, userRules);
        return analyzeBookingsRemote(analyticsRequest);
    }

    public ConfigStatus getAnalyticsConfigStatus() {
        ResponseEntity<ConfigStatus> responseEntity = smartanalyticsRestTemplate
            .getForEntity("/status", ConfigStatus.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }

        throw new SmartanalyticsException(responseEntity.getStatusCode(), null);
    }

    private AnalyticsResult analyzeBookingsRemote(AnalyticsRequest analyticsRequest) {
        ResponseEntity<EntityModel<AnalyticsResult>> responseEntity = smartanalyticsRestTemplate
            .exchange(
                "/api/v1/analytics", HttpMethod.PUT, new HttpEntity<Object>(analyticsRequest),
                    new ParameterizedTypeReference<>() {
                    }, Collections.emptyMap());

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody().getContent();
        }

        throw new SmartanalyticsException(responseEntity.getStatusCode(), null);
    }

    private AnalyticsRequest createAnalyticsRequest(List<Booking> bookings, List<Rule> customRules) {
        return AnalyticsRequest.builder()
            .bookings(bookings)
            .customRules(customRules)
            .build();
    }
}
