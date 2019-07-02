package de.adorsys.multibanking.jpa.entity;

import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "account_analytics")
@Data
public class AccountAnalyticsJpaEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String accountId;
    private String userId;
    private LocalDateTime analyticsDate = LocalDateTime.now();
    @Embedded
    private List<BookingGroupJpaEntity> bookingGroups;

}
