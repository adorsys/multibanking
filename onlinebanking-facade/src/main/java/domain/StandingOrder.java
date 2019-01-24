package domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 18.08.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "Standing order", value = "StandingOrder")
public class StandingOrder extends AbstractPayment {

    private Cycle cycle;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private BigDecimal amount;
    private BankAccount otherAccount;
    private String usage;

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.STANDING_ORDER;
    }

    @Override
    public String getSepaPain() {
        return null;
    }

}
