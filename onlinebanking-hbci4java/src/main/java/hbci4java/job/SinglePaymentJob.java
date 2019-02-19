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
import domain.FuturePayment;
import domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVTermUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.GVRTermUeb;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class SinglePaymentJob extends ScaRequiredJob {

    @Override
    protected AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                           String rawData) {
        SinglePayment singlePayment = (SinglePayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        AbstractSEPAGV sepagv;
        if (singlePayment instanceof FuturePayment) {
            sepagv = new GVTermUebSEPA(passport, GVTermUebSEPA.getLowlevelName(), rawData);
            sepagv.setParam("date", ((FuturePayment) singlePayment).getExecutionDate().toString());
        } else {
            sepagv = new GVUebSEPA(passport, GVUebSEPA.getLowlevelName(), rawData);
        }

        sepagv.setParam("src", src);
        sepagv.setParam("dst", dst);
        sepagv.setParam("btg", new Value(singlePayment.getAmount()));
        sepagv.setParam("usage", singlePayment.getPurpose());

        sepagv.verifyConstraints();

        return sepagv;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        if (paymentType == AbstractScaTransaction.TransactionType.FUTURE_PAYMENT) {
            return GVTermUebSEPA.getLowlevelName();
        }
        return GVUebSEPA.getLowlevelName();
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRTermUeb ? ((GVRTermUeb) paymentGV).getOrderId() : null; // no order id for
        // single payment
    }
}
