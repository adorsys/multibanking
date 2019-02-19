package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RawSepaPayment extends AbstractScaTransaction {

    private String painXml;
    private TransactionType sepaTransactionType;

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.RAW_SEPA;
    }

    @Override
    public String getRawData() {
        return painXml;
    }
}
