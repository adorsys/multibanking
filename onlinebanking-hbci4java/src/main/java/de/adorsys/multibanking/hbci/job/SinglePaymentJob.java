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
import de.adorsys.multibanking.domain.transaction.FutureSinglePayment;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import org.kapott.hbci.GV.*;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import static de.adorsys.multibanking.domain.transaction.AbstractTransaction.TransactionType.FUTURE_SINGLE_PAYMENT;

public class SinglePaymentJob extends AbstractPaymentJob<SinglePayment, AbstractHBCIJob> {

    public SinglePaymentJob(TransactionRequest<SinglePayment> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    AbstractHBCIJob createHbciJob() {
        SinglePayment singlePayment = transactionRequest.getTransaction();

        AbstractSEPAGV paympentJob;
        if (singlePayment instanceof FutureSinglePayment) {
            paympentJob = new GVTermUebSEPA(dialog.getPassport(), GVTermUebSEPA.getLowlevelName());
            paympentJob.setParam("date", ((FutureSinglePayment) singlePayment).getExecutionDate().toString());
        } else {
            if (singlePayment.isInstantPayment()) {
                paympentJob = new GVInstantUebSEPA(dialog.getPassport(), GVInstantUebSEPA.getLowlevelName());
            } else {
                paympentJob = new GVUebSEPA(dialog.getPassport(), GVUebSEPA.getLowlevelName());
            }
        }

        paympentJob.setParam("src", getHbciKonto());
        paympentJob.setParam("dst", createReceiverAccount(singlePayment));
        paympentJob.setParam("btg", new Value(singlePayment.getAmount(), singlePayment.getCurrency()));
        if (singlePayment.getPurpose() != null) {
            paympentJob.setParam("usage", singlePayment.getPurpose());
        }
        if (singlePayment.getPurposecode() != null) {
            paympentJob.setParam("purposecode", singlePayment.getPurposecode());
        }
        if (singlePayment.getEndToEndId() != null) {
            paympentJob.setParam("endtoendid", singlePayment.getEndToEndId());
        }

        return paympentJob;
    }

    private Konto createReceiverAccount(SinglePayment singlePayment) {
        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();
        return dst;
    }

    @Override
    protected String getHbciJobName() {
        if (transactionRequest.getTransaction().getTransactionType() == FUTURE_SINGLE_PAYMENT) {
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
