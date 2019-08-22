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

package de.adorsys.multibanking.domain.request;

import de.adorsys.multibanking.domain.BankAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
public class LoadBookingsRequest extends TransactionRequest {

    private String consentId;
    private BankAccount bankAccount;
    private RawResponseType rawResponseType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private boolean withBalance;
    private boolean withStandingOrders;

    public enum RawResponseType {
        CAMT, MT940
    }
}
