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

package de.adorsys.multibanking.jpa.entity;

import lombok.Data;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
@Data
public class BalancesReportEntity {

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "readyBalanceDate"))
    @AttributeOverride(name = "amount", column = @Column(name = "readyBalanceAmount"))
    @AttributeOverride(name = "currency", column = @Column(name = "readyBalanceCurrency"))
    private BalanceEntity readyBalance;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "unreadyBalanceDate"))
    @AttributeOverride(name = "amount", column = @Column(name = "unreadyBalanceAmount"))
    @AttributeOverride(name = "currency", column = @Column(name = "unreadyBalanceCurrency"))
    private BalanceEntity unreadyBalance;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "creditBalanceDate"))
    @AttributeOverride(name = "amount", column = @Column(name = "creditBalanceAmount"))
    @AttributeOverride(name = "currency", column = @Column(name = "creditBalanceCurrency"))
    private BalanceEntity creditBalance;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "availableBalanceDate"))
    @AttributeOverride(name = "amount", column = @Column(name = "availableBalanceAmount"))
    @AttributeOverride(name = "currency", column = @Column(name = "availableBalanceCurrency"))
    private BalanceEntity availableBalance;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "usedBalanceDate"))
    @AttributeOverride(name = "amount", column = @Column(name = "usedBalanceAmount"))
    @AttributeOverride(name = "currency", column = @Column(name = "usedBalanceCurrency"))
    private BalanceEntity usedBalance;

}
