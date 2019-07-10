package de.adorsys.multibanking.service.analytics;

import de.adorsys.smartanalytics.api.AnalyticsRequest;
import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.Rule;
import de.adorsys.smartanalytics.api.config.ConfigStatus;
import de.adorsys.smartanalytics.core.AnalyticsService;
import de.adorsys.smartanalytics.core.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Profile("smartanalytics-embedded")
public class SmartAnalyticsEmbeddedService implements SmartAnalyticsIf {

    private final StatusService statusService;
    private final AnalyticsService smartanalytics;

    public AnalyticsResult analyzeBookings(List<Rule> userRules, List<Booking> bookings) {
        AnalyticsRequest analyticsRequest = createAnalyticsRequest(bookings, userRules);
        return smartanalytics.analytics(analyticsRequest);
    }

    public ConfigStatus getAnalyticsConfigStatus() {
        return statusService.getStatus();
    }

    private AnalyticsRequest createAnalyticsRequest(List<Booking> bookings, List<Rule> customRules) {
        return AnalyticsRequest.builder()
            .bookings(bookings)
            .customRules(customRules)
            .build();
    }
}
