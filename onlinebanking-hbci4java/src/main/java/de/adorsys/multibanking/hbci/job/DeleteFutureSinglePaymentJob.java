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
import de.adorsys.multibanking.domain.transaction.FutureSinglePayment;
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTermUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@RequiredArgsConstructor
public class DeleteFutureSinglePaymentJob extends AbstractPaymentJob<FutureSinglePayment> {

    private final TransactionRequest<FutureSinglePayment> transactionRequest;
    private GVTermUebSEPADel hbciDeleteFutureSinglePaymentJob;
    private String jobName;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        FutureSinglePayment singlePayment = transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        jobName = GVTermUebSEPADel.getLowlevelName();

        hbciDeleteFutureSinglePaymentJob = new GVTermUebSEPADel(passport, jobName, null);

        hbciDeleteFutureSinglePaymentJob.setParam("orderid", singlePayment.getOrderId());
        hbciDeleteFutureSinglePaymentJob.setParam("date", singlePayment.getExecutionDate().toString());

        hbciDeleteFutureSinglePaymentJob.setParam("src", src);
        hbciDeleteFutureSinglePaymentJob.setParam("dst", dst);
        hbciDeleteFutureSinglePaymentJob.setParam("btg", new Value(singlePayment.getAmount(),
            singlePayment.getCurrency()));
        if (singlePayment.getPurpose() != null) {
            hbciDeleteFutureSinglePaymentJob.setParam("usage", singlePayment.getPurpose());
        }

        hbciDeleteFutureSinglePaymentJob.verifyConstraints();

        return hbciDeleteFutureSinglePaymentJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return hbciDeleteFutureSinglePaymentJob;
    }

    @Override
    TransactionRequest<FutureSinglePayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return jobName;
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }

}
