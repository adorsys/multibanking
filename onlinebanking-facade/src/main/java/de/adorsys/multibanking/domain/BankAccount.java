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

import lombok.Data;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Data
public class BankAccount {

    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);
    private Consent dedicatedConsent;
    private BalancesReport balances;
    private String owner;
    private String country;
    private String blz;
    private String bankName;
    private String accountNumber;
    private BankAccountType type;
    private String currency;
    private String name;
    private String bic;
    private String iban;
    private SyncStatus syncStatus;
    private LocalDateTime lastSync;

    public BankAccount balances(BalancesReport bankAccountBalance) {
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
            externalIdMap = new EnumMap<>(BankApi.class);
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
