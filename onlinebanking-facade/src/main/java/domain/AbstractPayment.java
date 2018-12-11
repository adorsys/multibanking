package domain;

import lombok.Data;

@Data
public abstract class AbstractPayment {

    private TanTransportType tanTransportType;
    private String senderAccountNumber;
    private String senderIban;
    private String senderBic;
    private String orderId;

    public abstract PaymentType getPaymentType();

    public enum PaymentType {
        SINGLE_PAYMENT,
        FUTURE_PAYMENT,
        BULK_PAYMENT,
        STANDING_ORDER
    }
}
