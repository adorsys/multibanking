/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package hbci4java.job;

import domain.AbstractScaTransaction;
import domain.BulkPayment;
import domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVMultiUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class BulkPaymentJob extends ScaRequiredJob {

    @Override
    protected AbstractSEPAGV createSepaJob(AbstractScaTransaction sepaTransaction, PinTanPassport passport, String sepaPain) {
        BulkPayment bulkPayment = (BulkPayment) sepaTransaction;

        Konto src = getOrderAccount(sepaTransaction, passport);

        GVUebSEPA uebSEPA = new GVMultiUebSEPA(passport, GVMultiUebSEPA.getLowlevelName(), sepaPain);
        uebSEPA.setParam("src", src);

        for (int i = 0; i < bulkPayment.getPayments().size(); i++) {
            SinglePayment payment = bulkPayment.getPayments().get(i);

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            uebSEPA.setParam("dst", i, dst);
            uebSEPA.setParam("btg", i, new Value(payment.getAmount()));
            uebSEPA.setParam("usage", i, payment.getPurpose());
        }

        uebSEPA.verifyConstraints();

        return uebSEPA;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return "MultiUebSEPA";
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null; // no orderId for bulk payment
    }
}
