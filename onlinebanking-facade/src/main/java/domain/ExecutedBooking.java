package domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ExecutedBooking {

    private String bookingId;
    private LocalDate executionDate;
    private boolean executed;
}
