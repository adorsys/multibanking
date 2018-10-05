package domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 19.10.17.
 */
@Data
public class SinglePayment extends AbstractPayment {

    private String receiver;
    private String receiverBic;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String purpose;
    private BigDecimal amount;
    private LocalDate executionDate;

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE_PAYMENT;
    }

}
