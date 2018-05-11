package de.adorsys.multibanking.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import domain.Contract;
import lombok.Builder;
import lombok.Data;

/**
 * Created by alexg on 09.08.17.
 */
@Data
@Builder
public class BookingGroup {

    public enum Type {
        STANDING_ORDER, RECURRENT_INCOME, RECURRENT_SEPA, RECURRENT_NONSEPA, CUSTOM, OTHER_INCOME, OTHER_EXPENSES
    }

    private Type type;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String otherAccount;
    private BigDecimal amount;
    private List<BookingPeriod> bookingPeriods;
    private Contract contract;
}
