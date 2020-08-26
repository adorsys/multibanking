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

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.PaymentResponse;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;

import java.util.Optional;

public abstract class AbstractPaymentJob<T extends AbstractPayment, J extends AbstractHBCIJob> extends ScaAwareJob<T, PaymentResponse> {

    public AbstractPaymentJob(TransactionRequest<T> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    protected PaymentResponse createJobResponse() {
        return new PaymentResponse(Optional.ofNullable(getTransactionId())
            .orElseGet(hbciTanSubmit::getOrderRef));
    }

    private String getTransactionId() {
        return Optional.ofNullable(getOrCreateHbciJob())
            .map(abstractHBCIJob -> orderIdFromJobResult(abstractHBCIJob.getJobResult()))
            .orElse(null);
    }

    public abstract String orderIdFromJobResult(HBCIJobResult paymentGV);
}
