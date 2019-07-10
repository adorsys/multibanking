package de.adorsys.multibanking.web.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingPeriodTO {

    private LocalDate start;
    private LocalDate end;
    private BigDecimal amount;
    private List<ExecutedBookingTO> bookings;
}
