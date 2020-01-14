package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "Booking group")
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
