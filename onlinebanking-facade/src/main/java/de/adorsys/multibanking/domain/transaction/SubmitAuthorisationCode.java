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
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static de.adorsys.multibanking.domain.transaction.AbstractScaTransaction.TransactionType.TAN_REQUEST;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorisationCode extends AbstractScaTransaction {

    private final AbstractScaTransaction originTransaction;

    @Override
    public TransactionType getTransactionType() {
        return Optional.ofNullable(originTransaction)
            .map(AbstractScaTransaction::getTransactionType)
            .orElse(TAN_REQUEST);
    }

    @Override
    public String getRawData() {
        return null;
    }
}
