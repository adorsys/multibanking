/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.multibanking.domain;

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
