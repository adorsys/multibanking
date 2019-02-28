package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FutureSinglePayment extends SinglePayment {

    private LocalDate executionDate;
    private boolean delete;

    @Override
    public void delete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public TransactionType getTransactionType() {
        if (delete) {
            return TransactionType.FUTURE_SINGLE_PAYMENT_DELETE;
        }
        return TransactionType.FUTURE_SINGLE_PAYMENT;
    }

}
