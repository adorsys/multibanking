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
import de.adorsys.multibanking.domain.transaction.TanRequest;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV_Result.HBCIJobResult;

public class TanRequestJob extends AbstractPaymentJob<TanRequest> {

    public TanRequestJob(TransactionRequest<TanRequest> transactionRequest, HbciBpdCacheHolder bpdCacheHolder) {
        super(transactionRequest, bpdCacheHolder);
    }

    @Override
    String getHbciJobName() {
        return null;
    }

    @Override
    AbstractSEPAGV createHbciJob() {
        return null;
    }

    @Override
    protected PaymentResponse createJobResponse() {
        return new PaymentResponse(null, null);
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }


}
