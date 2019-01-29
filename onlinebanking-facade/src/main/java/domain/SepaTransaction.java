package domain;

import lombok.Data;

@Data
public abstract class SepaTransaction {

    private TanTransportType tanTransportType;
    private String senderAccountNumber;
    private String senderIban;
    private String senderBic;
    private String orderId;

    public abstract TransactionType getTransactionType();

    public abstract String getSepaPain();

    public void delete(boolean delete) {
        if (delete) {
            throw new IllegalStateException("delete not supported");
        }
    }

    public enum TransactionType {
        SINGLE_PAYMENT,
        FUTURE_PAYMENT,
        FUTURE_PAYMENT_DELETE,
        BULK_PAYMENT,
        STANDING_ORDER,
        STANDING_ORDER_DELETE,
        RAW_SEPA
    }
}
