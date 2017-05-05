package domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by alexg on 08.02.17.
 */
@Data
public class BankAccountBalance {

    private BigDecimal readyHbciBalance;
    private BigDecimal unreadyHbciBalance;
    private BigDecimal creditHbciBalance;
    private BigDecimal availableHbciBalance;
    private BigDecimal usedHbciBalance;

    public BankAccountBalance readyHbciBalance(BigDecimal readyHbciBalance) {
        this.readyHbciBalance = readyHbciBalance;
        return this;
    }

    public BankAccountBalance unreadyHbciBalance(BigDecimal unreadyHbciBalance) {
        this.unreadyHbciBalance = unreadyHbciBalance;
        return this;
    }

    public BankAccountBalance creditHbciBalance(BigDecimal creditHbciBalance) {
        this.creditHbciBalance = creditHbciBalance;
        return this;
    }

    public BankAccountBalance availableHbciBalance(BigDecimal availableHbciBalance) {
        this.availableHbciBalance = availableHbciBalance;
        return this;
    }

    public BankAccountBalance usedHbciBalance(BigDecimal usedHbciBalance) {
        this.usedHbciBalance = usedHbciBalance;
        return this;
    }
}
