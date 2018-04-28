package de.adorsys.multibanking.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import domain.Contract;
import lombok.Builder;
import lombok.Data;

/**
 * Created by alexg on 09.08.17.
 */
@Data
@Builder
public class BookingGroup {

    private boolean variable;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String otherAccount;
    private BigDecimal amount;
    private LocalDate nextExecutionDate;
    private Contract contract;
}
