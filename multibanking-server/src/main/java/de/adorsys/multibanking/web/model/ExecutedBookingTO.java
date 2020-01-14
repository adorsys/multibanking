package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Schema(name = "Executed Booking")
@Data
public class ExecutedBookingTO {

    private String bookingId;
    private LocalDate executionDate;
    private boolean executed;
}
