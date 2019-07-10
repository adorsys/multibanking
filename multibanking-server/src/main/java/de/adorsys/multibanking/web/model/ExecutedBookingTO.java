package de.adorsys.multibanking.web.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExecutedBookingTO {

    private String bookingId;
    private LocalDate executionDate;
    private boolean executed;
}
