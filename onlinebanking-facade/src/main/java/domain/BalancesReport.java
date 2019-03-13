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
    private Balance readyBalance;

    @ApiModelProperty(value = "Unreleased account balance")
    private Balance unreadyBalance;

    @ApiModelProperty(value = "Credit balance")
    private Balance creditBalance;

    @ApiModelProperty(value = "Available balance")
    private Balance availableBalance;

    @ApiModelProperty(value = "Used balance")
    private Balance usedBalance;

    public BalancesReport readyHbciBalance(Balance readyHbciBalance) {
        this.readyBalance = readyHbciBalance;
        return this;
    }

    public BalancesReport unreadyHbciBalance(Balance unreadyHbciBalance) {
        this.unreadyBalance = unreadyHbciBalance;
        return this;
    }

    public BalancesReport creditHbciBalance(Balance creditHbciBalance) {
        this.creditBalance = creditHbciBalance;
        return this;
    }

    public BalancesReport availableHbciBalance(Balance availableHbciBalance) {
        this.availableBalance = availableHbciBalance;
        return this;
    }

    public BalancesReport usedHbciBalance(Balance usedHbciBalance) {
        this.usedBalance = usedHbciBalance;
        return this;
    }
}
