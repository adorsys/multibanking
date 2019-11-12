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
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTermMultiUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;

/**
 * Only for future payment (GVTermUebSEPA)
 */
@RequiredArgsConstructor
public class DeleteFutureBulkPaymentJob extends AbstractPaymentJob<FutureBulkPayment> {

    private final TransactionRequest<FutureBulkPayment> transactionRequest;
    private GVTermMultiUebSEPADel hbciDeleteFutureBulkPaymentJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        FutureBulkPayment futureBulkPayment = transactionRequest.getTransaction();

        hbciDeleteFutureBulkPaymentJob = new GVTermMultiUebSEPADel(passport, GVTermMultiUebSEPADel.getLowlevelName());

        hbciDeleteFutureBulkPaymentJob.setParam("orderid", futureBulkPayment.getOrderId());
        hbciDeleteFutureBulkPaymentJob.setParam("src", getPsuKonto(passport));
        hbciDeleteFutureBulkPaymentJob.verifyConstraints();

        return hbciDeleteFutureBulkPaymentJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return hbciDeleteFutureBulkPaymentJob;
    }

    @Override
    TransactionRequest<FutureBulkPayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return "TermMultiUebSEPADel";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
