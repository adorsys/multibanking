/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FutureBulkPayment extends BulkPayment {

    private LocalDate executionDate;
    private boolean delete;

    @Override
    public void delete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public TransactionType getTransactionType() {
        if (delete) {
            return TransactionType.FUTURE_BULK_PAYMENT_DELETE;
        }
        return TransactionType.FUTURE_BULK_PAYMENT;
    }

}
