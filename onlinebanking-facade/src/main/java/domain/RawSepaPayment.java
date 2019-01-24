package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RawSepaPayment extends AbstractPayment {

    private String painXml;

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.RAW_SEPA;
    }

    @Override
    public String getSepaPain() {
        return painXml;
    }
}
