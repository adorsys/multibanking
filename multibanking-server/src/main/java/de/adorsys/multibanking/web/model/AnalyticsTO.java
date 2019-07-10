package de.adorsys.multibanking.web.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnalyticsTO {

    private LocalDateTime analyticsDate;
    private List<BookingGroupTO> bookingGroups;
}
