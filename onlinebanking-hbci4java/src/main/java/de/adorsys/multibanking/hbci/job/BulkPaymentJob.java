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
import de.adorsys.multibanking.domain.transaction.BulkPayment;
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import org.apache.commons.lang3.BooleanUtils;
import org.kapott.hbci.GV.GVMultiUebSEPA;
import org.kapott.hbci.GV.GVTermMultiUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import static de.adorsys.multibanking.domain.transaction.AbstractTransaction.TransactionType.FUTURE_BULK_PAYMENT;

public class BulkPaymentJob extends AbstractPaymentJob<BulkPayment, GVUebSEPA> {

    public BulkPaymentJob(TransactionRequest<BulkPayment> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    GVUebSEPA createHbciJob() {
        BulkPayment bulkPayment = transactionRequest.getTransaction();

        Konto src = getHbciKonto();

        GVUebSEPA hbciJob;
        if (bulkPayment instanceof FutureBulkPayment) {
            hbciJob = new GVTermMultiUebSEPA(dialog.getPassport(), GVTermMultiUebSEPA.getLowlevelName());
            hbciJob.setParam("date", ((FutureBulkPayment) bulkPayment).getExecutionDate().toString());
        } else {
            hbciJob = new GVMultiUebSEPA(dialog.getPassport(), GVMultiUebSEPA.getLowlevelName());
        }

        hbciJob.setParam("src", src);
        hbciJob.setParam("batchbook", BooleanUtils.isTrue(bulkPayment.getBatchbooking()) ? "1" : "0");

        for (int i = 0; i < bulkPayment.getPayments().size(); i++) {
            SinglePayment payment = bulkPayment.getPayments().get(i);

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            hbciJob.setParam("dst", i, dst);
            hbciJob.setParam("btg", i, new Value(payment.getAmount(), payment.getCurrency()));
            if (payment.getPurpose() != null) {
                hbciJob.setParam("usage", i, payment.getPurpose());
            }
            if (payment.getPurposecode() != null) {
                hbciJob.setParam("purposecode", i, payment.getPurposecode());
            }
            if (payment.getEndToEndId() != null) {
                hbciJob.setParam("endtoendid", i, payment.getEndToEndId());
            }
        }

        return hbciJob;
    }

    @Override
    protected String getHbciJobName() {
        if (transactionRequest.getTransaction().getTransactionType() == FUTURE_BULK_PAYMENT) {
            return "TermMultiUebSEPA";
        }
        return "MultiUebSEPA";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRPayment ? ((GVRPayment) paymentGV).getOrderId() : null;
    }
}
