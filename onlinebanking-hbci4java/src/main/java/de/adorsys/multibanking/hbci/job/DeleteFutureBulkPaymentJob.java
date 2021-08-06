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
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import org.kapott.hbci.GV.GVTermMultiUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;

public class DeleteFutureBulkPaymentJob extends AbstractPaymentJob<FutureBulkPayment> {

    public DeleteFutureBulkPaymentJob(TransactionRequest<FutureBulkPayment> transactionRequest, HbciBpdCacheHolder bpdCacheHolder) {
        super(transactionRequest, bpdCacheHolder);
    }

    @Override
    GVTermMultiUebSEPADel createHbciJob() {
        FutureBulkPayment futureBulkPayment = transactionRequest.getTransaction();

        GVTermMultiUebSEPADel hbciJob = new GVTermMultiUebSEPADel(dialog.getPassport(), GVTermMultiUebSEPADel.getLowlevelName());

        hbciJob.setParam("orderid", futureBulkPayment.getOrderId());
        hbciJob.setParam("src", getHbciKonto());
        hbciJob.verifyConstraints();

        return hbciJob;
    }

    @Override
    protected String getHbciJobName() {
        return "TermMultiUebSEPADel";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
