package de.adorsys.multibanking.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BalancesReportTO {
    @ApiModelProperty(value = "Ready account balance")
    private BalanceTO readyBalance;

    @ApiModelProperty(value = "Unreleased account balance")
    private BalanceTO unreadyBalance;

    @ApiModelProperty(value = "Credit balance")
    private BalanceTO creditBalance;

    @ApiModelProperty(value = "Available balance")
    private BalanceTO availableBalance;

    @ApiModelProperty(value = "Used balance")
    private BalanceTO usedBalance;
}
