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
public class StandingOrder extends AbstractScaType {
    private String orderId;
    private Cycle cycle;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private BigDecimal amount;
    private String senderAccountNumber;
    private String senderIban;
    private String senderBic;
    private BankAccount otherAccount;
    private String usage;
}
