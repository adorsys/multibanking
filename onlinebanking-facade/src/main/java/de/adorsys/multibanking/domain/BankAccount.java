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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description = "The bank account object", value = "BankAccount")
public class BankAccount {

    @ApiModelProperty(hidden = true)
    private Map<BankApi, String> externalIdMap = new HashMap<>();
    @ApiModelProperty(value = "Bank account balances")
    private BalancesReport balances;
    @ApiModelProperty(value = "Name of the account owner", example = "EDEKA")
    private String owner;
    @ApiModelProperty(value = "ISO-2 country of this bank account", example = "DE")
    private String country;
    @ApiModelProperty(value = "Bank code", example = "29999999")
    private String blz;
    @ApiModelProperty(value = "Bank name", example = "Mock Bank")
    private String bankName;
    @ApiModelProperty(value = "Account number", example = "1234567890")
    private String accountNumber;
    @ApiModelProperty(value = "Type of this bank account", example = "GIRO")
    private BankAccountType type;
    @ApiModelProperty(value = "Currency of this bank account", example = "EURO")
    private String currency;
    @ApiModelProperty(value = "Name of this bank account")
    private String name;
    @ApiModelProperty(value = "Bank identification code", example = "EDEKDEHHXXX")
    private String bic;
    @ApiModelProperty(value = "International bank account number", example = "DE50200907003443582071", required = true)
    private String iban;
    @ApiModelProperty(value = "Synchronisation status", example = "PENDING")
    private SyncStatus syncStatus;
    @ApiModelProperty(value = "Last Synchronisation date", example = "2017-12-01")
    private LocalDateTime lastSync;

    public BankAccount bankAccountBalance(BalancesReport bankAccountBalance) {
        this.balances = bankAccountBalance;
        return this;
    }

    public BankAccount country(String country) {
        this.country = country;
        return this;
    }

    public BankAccount blz(String blz) {
        this.blz = blz;
        return this;
    }

    public BankAccount accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public BankAccount type(BankAccountType type) {
        this.type = type;
        return this;
    }

    public BankAccount currency(String currency) {
        this.currency = currency;
        return this;
    }

    public BankAccount name(String name) {
        this.name = name;
        return this;
    }

    public BankAccount bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public BankAccount bic(String bic) {
        this.bic = bic;
        return this;
    }

    public BankAccount iban(String iban) {
        this.iban = iban;
        return this;
    }

    public BankAccount externalId(BankApi bankApi, String externalId) {
        if (externalIdMap == null) {
            externalIdMap = new HashMap<>();
        }
        externalIdMap.put(bankApi, externalId);
        return this;
    }

    public BankAccount owner(String owner) {
        this.owner = owner;
        return this;
    }

    public enum SyncStatus {
        PENDING, SYNC, READY
    }

}
