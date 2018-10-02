package domain;

import lombok.Data;

@Data
public abstract class AbstractPayment {

    public enum PaymentType {
        SINGLE_PAYMENT,
        BULK_PAYMENT,
        STANDING_ORDER
    }

    private TanTransportType tanMedia;
    private PaymentChallenge paymentChallenge;

    private String senderAccountNumber;
    private String senderIban;
    private String senderBic;

    public abstract PaymentType getPaymentType();
}
