package de.adorsys.multibanking.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookingPeriod {

    private LocalDate start;
    private LocalDate end;
    private List<LocalDate> bookingDates;
}
