package domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by alexg on 08.02.17.
 */
@Data
@ApiModel(description = "The balances of this bank account", value = "BankAccountBalances")
public class BalancesReport {

    @ApiModelProperty(value = "Ready account balance")
    private Balance readyHbciBalance;

    @ApiModelProperty(value = "Unreleased account balance")
    private Balance unreadyHbciBalance;

    @ApiModelProperty(value = "Credit balance")
    private Balance creditHbciBalance;

    @ApiModelProperty(value = "Available balance")
    private Balance availableHbciBalance;

    @ApiModelProperty(value = "Used balance")
    private Balance usedHbciBalance;

    public BalancesReport readyHbciBalance(Balance readyHbciBalance) {
        this.readyHbciBalance = readyHbciBalance;
        return this;
    }

    public BalancesReport unreadyHbciBalance(Balance unreadyHbciBalance) {
        this.unreadyHbciBalance = unreadyHbciBalance;
        return this;
    }

    public BalancesReport creditHbciBalance(Balance creditHbciBalance) {
        this.creditHbciBalance = creditHbciBalance;
        return this;
    }

    public BalancesReport availableHbciBalance(Balance availableHbciBalance) {
        this.availableHbciBalance = availableHbciBalance;
        return this;
    }

    public BalancesReport usedHbciBalance(Balance usedHbciBalance) {
        this.usedHbciBalance = usedHbciBalance;
        return this;
    }
}
