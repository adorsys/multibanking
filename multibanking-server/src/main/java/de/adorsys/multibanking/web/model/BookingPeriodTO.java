package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "BookingPeriod")
@Data
public class BookingPeriodTO {

    private LocalDate start;
    private LocalDate end;
    private BigDecimal amount;
    private List<ExecutedBookingTO> bookings;
}
