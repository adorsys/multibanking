package domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 19.10.17.
 */
@Data
public class Payment {

    public enum PaymentType {
        TRANSFER,
        SEPA_TRANSFER,
        STANDING_ORDER
    }

    private String accessToken;
    private String taskToken;

    private PaymentType paymentType;
    private String receiver;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String purpose;
    private BigDecimal amount;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private Cycle cycle;


}
