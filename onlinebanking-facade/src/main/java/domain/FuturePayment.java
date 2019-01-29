package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuturePayment extends SinglePayment {

    private LocalDate executionDate;
    private boolean delete;

    @Override
    public void delete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.FUTURE_PAYMENT;
    }

    @Override
    public String getSepaPain() {
        return null;
    }

}
