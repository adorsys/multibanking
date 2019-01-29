package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TanRequest extends AbstractScaTransaction {

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.TAN_REQUEST;
    }

    @Override
    public String getSepaPain() {
        return null;
    }

}
