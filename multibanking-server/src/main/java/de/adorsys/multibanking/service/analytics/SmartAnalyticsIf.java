package de.adorsys.multibanking.service.analytics;

import de.adorsys.smartanalytics.api.AnalyticsResult;
import de.adorsys.smartanalytics.api.Booking;
import de.adorsys.smartanalytics.api.Rule;
import de.adorsys.smartanalytics.api.config.ConfigStatus;

import java.util.List;

public interface SmartAnalyticsIf {

    AnalyticsResult analyzeBookings(List<Rule> userRules, List<Booking> bookings);

    ConfigStatus getAnalyticsConfigStatus();
}
