package domain;

import lombok.Data;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by alexg on 08.02.17.
 */
@Data
@ApiModel(description="The balance of this bank account.", value="BankAccountBalance" )
public class BankAccountBalance {

	@ApiModelProperty(value = "The ready account balance")
    private BigDecimal readyHbciBalance;

	@ApiModelProperty(value = "The unreleased account balance")
	private BigDecimal unreadyHbciBalance;

	@ApiModelProperty(value = "The credit balance")
	private BigDecimal creditHbciBalance;

	@ApiModelProperty(value = "The available balance")
	private BigDecimal availableHbciBalance;

	@ApiModelProperty(value = "The used balance")
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
