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

import de.adorsys.multibanking.domain.request.TransactionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static de.adorsys.multibanking.domain.transaction.AbstractScaTransaction.TransactionType.TAN_REQUEST;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class SubmitAuthorisationCode<T extends AbstractScaTransaction> extends AbstractScaTransaction {

    private final TransactionRequest<T> originTransactionRequest;

    @Override
    public TransactionType getTransactionType() {
        return Optional.ofNullable(originTransactionRequest)
            .map(TransactionRequest::getTransaction)
            .map(AbstractScaTransaction::getTransactionType)
            .orElse(TAN_REQUEST);
    }

    @Override
    public String getRawData() {
        return null;
    }
}
