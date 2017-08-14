package de.adorsys.multibanking.domain;

import domain.Contract;
import domain.Cycle;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by alexg on 09.08.17.
 */
@Data
@Builder
public class BookingGroup {

    boolean variable;
    String mainCategory;
    String subCategory;
    String specification;
    String otherAccount;
    private BigDecimal amount;
    private LocalDate nextExecutionDate;
    Contract contract;
}
