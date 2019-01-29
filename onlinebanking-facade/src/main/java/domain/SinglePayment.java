package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SinglePayment extends SepaTransaction {

    private String receiver;
    private String receiverBic;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String purpose;
    private BigDecimal amount;

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.SINGLE_PAYMENT;
    }

    @Override
    public String getSepaPain() {
        return null;
    }

}
