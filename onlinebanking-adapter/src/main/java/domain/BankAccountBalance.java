package domain;

import java.math.BigDecimal;

/**
 * Created by alexg on 08.02.17.
 */
public class BankAccountBalance {

    private BigDecimal readyHbciBalance;
    private BigDecimal unreadyHbciBalance;
    private BigDecimal creditHbciBalance;
    private BigDecimal availableHbciBalance;
    private BigDecimal usedHbciBalance;

    public BigDecimal getReadyHbciBalance() {
        return readyHbciBalance;
    }

    public BankAccountBalance readyHbciBalance(BigDecimal readyHbciBalance) {
        this.readyHbciBalance = readyHbciBalance;
        return this;
    }

    public BigDecimal getUnreadyHbciBalance() {
        return unreadyHbciBalance;
    }

    public BankAccountBalance unreadyHbciBalance(BigDecimal unreadyHbciBalance) {
        this.unreadyHbciBalance = unreadyHbciBalance;
        return this;
    }

    public BigDecimal getCreditHbciBalance() {
        return creditHbciBalance;
    }

    public BankAccountBalance creditHbciBalance(BigDecimal creditHbciBalance) {
        this.creditHbciBalance = creditHbciBalance;
        return this;
    }

    public BigDecimal getAvailableHbciBalance() {
        return availableHbciBalance;
    }

    public BankAccountBalance availableHbciBalance(BigDecimal availableHbciBalance) {
        this.availableHbciBalance = availableHbciBalance;
        return this;
    }

    public BigDecimal getUsedHbciBalance() {
        return usedHbciBalance;
    }

    public BankAccountBalance usedHbciBalance(BigDecimal usedHbciBalance) {
        this.usedHbciBalance = usedHbciBalance;
        return this;
    }
}
