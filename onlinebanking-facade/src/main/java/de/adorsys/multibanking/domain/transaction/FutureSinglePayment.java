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

import java.time.LocalDate;

import static de.adorsys.multibanking.domain.transaction.AbstractTransaction.TransactionType.FUTURE_SINGLE_PAYMENT;
import static de.adorsys.multibanking.domain.transaction.AbstractTransaction.TransactionType.FUTURE_SINGLE_PAYMENT_DELETE;

/**
 * Created by alexg on 19.10.17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FutureSinglePayment extends SinglePayment implements DeletablePayment {

    private LocalDate executionDate;
    private boolean delete;

    @Override
    public TransactionType getTransactionType() {
        return delete ? FUTURE_SINGLE_PAYMENT_DELETE : FUTURE_SINGLE_PAYMENT;
    }

}
