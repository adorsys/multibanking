package de.adorsys.multibanking.web.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookingGroupTO {

    private Type type;
    private String name;
    private boolean salaryWage;
    private String mainCategory;
    private String subCategory;
    private String specification;
    private String otherAccount;
    private BigDecimal amount;
    private List<BookingPeriodTO> bookingPeriods;
    private ContractTO contract;

    public enum Type {
        STANDING_ORDER, RECURRENT_INCOME, RECURRENT_SEPA, RECURRENT_NONSEPA, CUSTOM, OTHER_INCOME, OTHER_EXPENSES
    }
}
