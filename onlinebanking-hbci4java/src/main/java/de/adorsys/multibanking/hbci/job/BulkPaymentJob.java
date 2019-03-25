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

import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.BulkPayment;
import de.adorsys.multibanking.domain.FutureBulkPayment;
import de.adorsys.multibanking.domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVMultiUebSEPA;
import org.kapott.hbci.GV.GVTermMultiUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.GVRTermUeb;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class BulkPaymentJob extends ScaRequiredJob {

    @Override
    protected AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                           String rawData) {
        BulkPayment bulkPayment = (BulkPayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        AbstractSEPAGV sepagv;
        if (bulkPayment instanceof FutureBulkPayment) {
            sepagv = new GVTermMultiUebSEPA(passport, GVTermMultiUebSEPA.getLowlevelName(), rawData);
            sepagv.setParam("date", ((FutureBulkPayment) bulkPayment).getExecutionDate().toString());
        } else {
            sepagv = new GVMultiUebSEPA(passport, GVUebSEPA.getLowlevelName(), rawData);
        }

        sepagv.setParam("src", src);

        for (int i = 0; i < bulkPayment.getPayments().size(); i++) {
            SinglePayment payment = bulkPayment.getPayments().get(i);

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            sepagv.setParam("dst", i, dst);
            sepagv.setParam("btg", i, new Value(payment.getAmount(), payment.getCurrency()));
            sepagv.setParam("usage", i, payment.getPurpose());
        }

        sepagv.verifyConstraints();

        return sepagv;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        if (paymentType == AbstractScaTransaction.TransactionType.FUTURE_BULK_PAYMENT) {
            return "TermMultiUebSEPA";
        }
        return "MultiUebSEPA";
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRTermUeb ? ((GVRTermUeb) paymentGV).getOrderId() : null;
    }
}
