package de.adorsys.multibanking.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "Balances report")
@Data
public class BalancesReportTO {

    @Schema(description = "Ready account balance")
    private BalanceTO readyBalance;

    @Schema(description = "Unreleased account balance")
    private BalanceTO unreadyBalance;

    @Schema(description = "Credit balance")
    private BalanceTO creditBalance;

    @Schema(description = "Available balance")
    private BalanceTO availableBalance;

    @Schema(description = "Used balance")
    private BalanceTO usedBalance;
}
