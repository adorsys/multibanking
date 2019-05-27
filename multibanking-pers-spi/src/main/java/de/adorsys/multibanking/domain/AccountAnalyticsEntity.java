package de.adorsys.multibanking.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AccountAnalyticsEntity {

    private String id;
    private String accountId;
    private String userId;

    private LocalDateTime analyticsDate = LocalDateTime.now();

    private List<BookingGroup> bookingGroups;

}
