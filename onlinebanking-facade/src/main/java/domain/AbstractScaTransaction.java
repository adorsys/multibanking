package domain;

import lombok.Data;

@Data
public abstract class AbstractScaTransaction {

    private BankAccount debtorBankAccount;
    private String orderId;
    private String paymentId;

    public abstract TransactionType getTransactionType();

    public abstract String getRawData();

    public void delete(boolean delete) {
        if (delete) {
            throw new IllegalStateException("delete not supported");
        }
    }

    public enum TransactionType {
        SINGLE_PAYMENT,
        FOREIGN_PAYMENT,
        FUTURE_PAYMENT,
        FUTURE_PAYMENT_DELETE,
        BULK_PAYMENT,
        STANDING_ORDER,
        STANDING_ORDER_DELETE,
        RAW_SEPA,
        TAN_REQUEST
    }
}
