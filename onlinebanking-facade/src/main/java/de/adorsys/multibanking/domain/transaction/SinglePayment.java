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

package de.adorsys.multibanking.domain.transaction;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class SinglePayment extends AbstractPayment {

    private String receiver;
    private String receiverBic;
    private String receiverIban;
    private String receiverBankCode;
    private String receiverAccountNumber;
    private String receiverAccountCurrency;
    private String purpose;
    private String purposecode;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime submittedTime;
    private boolean instantPayment;
    private boolean transfer;

    @Override
    public TransactionType getTransactionType() {
        if (transfer) {
            return TransactionType.TRANSFER_PAYMENT;
        } else if (instantPayment) {
            return TransactionType.INSTANT_PAYMENT;
        } else {
            return TransactionType.SINGLE_PAYMENT;
        }
    }

}
