package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ForeignPayment extends AbstractScaTransaction {

    private String dtazv;

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.FOREIGN_PAYMENT;
    }

    @Override
    public String getRawData() {
        return dtazv;
    }

}
