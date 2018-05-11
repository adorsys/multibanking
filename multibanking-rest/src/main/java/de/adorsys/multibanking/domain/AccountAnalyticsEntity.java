package de.adorsys.multibanking.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.adorsys.multibanking.domain.common.AbstractId;
import lombok.Data;

/**
 * Created by alexg on 08.05.17.
 */
@Data
public class AccountAnalyticsEntity extends AbstractId {

    private String accountId;
    private String userId;

    private LocalDate analyticsDate = LocalDate.now();

    private BigDecimal incomeTotal = new BigDecimal(0);

    private BigDecimal incomeFix = new BigDecimal(0);
    private List<BookingGroup> incomeFixBookings = new ArrayList<>();
    private BigDecimal incomeVariable = new BigDecimal(0);
    private List<BookingGroup> incomeVariableBookings  = new ArrayList<>();
    private BigDecimal incomeNext = new BigDecimal(0);
    private List<BookingGroup> incomeNextBookings = new ArrayList<>();

    private BigDecimal expensesTotal = new BigDecimal(0);

    private BigDecimal expensesFix = new BigDecimal(0);
    private List<BookingGroup> expensesFixBookings = new ArrayList<>();
    private BigDecimal expensesVariable = new BigDecimal(0);
    private List<BookingGroup> expensesVariableBookings = new ArrayList<>();
    private BigDecimal expensesNext = new BigDecimal(0);
    private List<BookingGroup> expensesNextBookings = new ArrayList<>();

    private BigDecimal balanceCalculated = new BigDecimal(0);

}
