package domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 18.08.17.
 */
@Data
@ApiModel(description = "Standing order", value = "StandingOrder")
public class StandingOrder {

    public enum Cycle {
        TWO_WEEKLY,
        MONTHLY
    }

    private String orderId;
    private Cycle cycle;
    private int executionDay;
    private LocalDate firstBookingDate;
    private LocalDate lastBookingDate;
    private BigDecimal amount;
    private BankAccount otherAccount;
    private String usage;

}
