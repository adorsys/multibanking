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
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.*;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@RequiredArgsConstructor
public class SinglePaymentJob extends AbstractPaymentJob<SinglePayment> {

    private final TransactionRequest<SinglePayment> transactionRequest;
    private AbstractSEPAGV hbciSinglePaymentJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        SinglePayment singlePayment = transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        if (singlePayment instanceof FutureSinglePayment) {
            hbciSinglePaymentJob = new GVTermUebSEPA(passport, GVTermUebSEPA.getLowlevelName());
            hbciSinglePaymentJob.setParam("date", ((FutureSinglePayment) singlePayment).getExecutionDate().toString());
        } else {
            if (singlePayment.isInstantPayment()) {
                hbciSinglePaymentJob = new GVInstantUebSEPA(passport, GVInstantUebSEPA.getLowlevelName());
            } else {
                hbciSinglePaymentJob = new GVUebSEPA(passport, GVUebSEPA.getLowlevelName());
            }
        }

        hbciSinglePaymentJob.setParam("src", src);
        hbciSinglePaymentJob.setParam("dst", dst);
        hbciSinglePaymentJob.setParam("btg", new Value(singlePayment.getAmount(), singlePayment.getCurrency()));
        if (singlePayment.getPurpose() != null) {
            hbciSinglePaymentJob.setParam("usage", singlePayment.getPurpose());
        }
        if (singlePayment.getPurposecode() != null) {
            hbciSinglePaymentJob.setParam("purposecode", singlePayment.getPurposecode());
        }
        hbciSinglePaymentJob.verifyConstraints();

        return hbciSinglePaymentJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return hbciSinglePaymentJob;
    }

    @Override
    TransactionRequest<SinglePayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        if (transactionType == AbstractTransaction.TransactionType.FUTURE_SINGLE_PAYMENT) {
            return GVTermUebSEPA.getLowlevelName();
        }
        return GVUebSEPA.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRPayment ? ((GVRPayment) paymentGV).getOrderId() : null; // no order id for
        // single payment
    }
}
