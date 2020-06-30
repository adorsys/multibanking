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
import de.adorsys.multibanking.domain.transaction.BulkPayment;
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVMultiUebSEPA;
import org.kapott.hbci.GV.GVTermMultiUebSEPA;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@RequiredArgsConstructor
public class BulkPaymentJob extends AbstractPaymentJob<BulkPayment> {

    private final TransactionRequest<BulkPayment> transactionRequest;
    private AbstractSEPAGV bulkPaymentHbciJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        BulkPayment bulkPayment = transactionRequest.getTransaction();

        Konto src = getHbciKonto(passport);

        if (bulkPayment instanceof FutureBulkPayment) {
            bulkPaymentHbciJob = new GVTermMultiUebSEPA(passport, GVTermMultiUebSEPA.getLowlevelName());
            bulkPaymentHbciJob.setParam("date", ((FutureBulkPayment) bulkPayment).getExecutionDate().toString());
        } else {
            bulkPaymentHbciJob = new GVMultiUebSEPA(passport, GVMultiUebSEPA.getLowlevelName());
        }

        bulkPaymentHbciJob.setParam("src", src);
        bulkPaymentHbciJob.setParam("batchbook", BooleanUtils.isTrue(bulkPayment.getBatchbooking()) ? "1" : "0");

        for (int i = 0; i < bulkPayment.getPayments().size(); i++) {
            SinglePayment payment = bulkPayment.getPayments().get(i);

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            bulkPaymentHbciJob.setParam("dst", i, dst);
            bulkPaymentHbciJob.setParam("btg", i, new Value(payment.getAmount(), payment.getCurrency()));
            if (payment.getPurpose() != null) {
                bulkPaymentHbciJob.setParam("usage", i, payment.getPurpose());
            }
            if (payment.getPurposecode() != null) {
                bulkPaymentHbciJob.setParam("purposecode", i, payment.getPurposecode());
            }
            if (payment.getEndToEndId() != null) {
                bulkPaymentHbciJob.setParam("endtoendid", i, payment.getEndToEndId());
            }
        }

        bulkPaymentHbciJob.verifyConstraints();

        return bulkPaymentHbciJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return bulkPaymentHbciJob;
    }

    @Override
    TransactionRequest<BulkPayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        if (transactionType == AbstractTransaction.TransactionType.FUTURE_BULK_PAYMENT) {
            return "TermMultiUebSEPA";
        }
        return "MultiUebSEPA";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRPayment ? ((GVRPayment) paymentGV).getOrderId() : null;
    }
}
