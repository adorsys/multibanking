package domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private TanTransportType tanMedia;

    private PaymentChallenge paymentChallenge;
    private PaymentType paymentType;

    private String receiver;
    private String receiverBic;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String purpose;
    private BigDecimal amount;
    private int executionDay;
    private LocalDate firstExecutionDate;
    private LocalDate lastExecutionDate;
    private Cycle cycle;

    private Object tanSubmitExternal;




}
