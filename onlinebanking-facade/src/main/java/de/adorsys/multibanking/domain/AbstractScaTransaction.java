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

@Data
public abstract class AbstractScaTransaction {

    private BankAccount debtorBankAccount;
    private String orderId;
    private String paymentId;

    public abstract TransactionType getTransactionType();

    public abstract String getRawData();

    public void delete(boolean delete) {
        if (delete) {
            throw new IllegalStateException("delete not supported");
        }
    }

    public enum TransactionType {
        SINGLE_PAYMENT,
        FOREIGN_PAYMENT,
        FUTURE_SINGLE_PAYMENT,
        FUTURE_SINGLE_PAYMENT_DELETE,
        BULK_PAYMENT,
        FUTURE_BULK_PAYMENT,
        FUTURE_BULK_PAYMENT_DELETE,
        STANDING_ORDER,
        STANDING_ORDER_DELETE,
        RAW_SEPA,
        TAN_REQUEST,
        DEDICATED_CONSENT
    }
}
