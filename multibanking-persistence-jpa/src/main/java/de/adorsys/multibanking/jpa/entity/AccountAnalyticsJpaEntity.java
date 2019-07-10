package de.adorsys.multibanking.jpa.entity;

import lombok.Data;

import javax.persistence.*;
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
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "analytics_bookinggroup",
            joinColumns = @JoinColumn(name = "accountanalytics_id"))
    private List<BookingGroupJpaEntity> bookingGroups;

}
