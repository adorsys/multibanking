package domain;

import lombok.Data;

@Data
public abstract class AbstractPayment {

    public enum PaymentType {
        SINGLE_PAYMENT,
        FUTURE_PAYMENT,
        BULK_PAYMENT,
        STANDING_ORDER
    }

    private TanTransportType tanMedia;

    private String senderAccountNumber;
    private String senderIban;
    private String senderBic;

    private String orderId;

    public abstract PaymentType getPaymentType();
}
