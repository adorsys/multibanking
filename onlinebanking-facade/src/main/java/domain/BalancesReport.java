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

    public BalancesReport readyBalance(Balance readyBalance) {
        this.readyBalance = readyBalance;
        return this;
    }

    public BalancesReport unreadyBalance(Balance unreadyBalance) {
        this.unreadyBalance = unreadyBalance;
        return this;
    }

    public BalancesReport creditBalance(Balance creditBalance) {
        this.creditBalance = creditBalance;
        return this;
    }

    public BalancesReport availableBalance(Balance availableBalance) {
        this.availableBalance = availableBalance;
        return this;
    }

    public BalancesReport usedBalance(Balance usedBalance) {
        this.usedBalance = usedBalance;
        return this;
    }
}
