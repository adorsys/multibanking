package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "Analytics")
@Data
public class AnalyticsTO {

    private LocalDateTime analyticsDate;
    private List<BookingGroupTO> bookingGroups;
}
