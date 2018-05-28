package domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookingPeriod {

    private LocalDate start;
    private LocalDate end;
    private BigDecimal amount;
    private List<ExecutedBooking> bookings;
}
