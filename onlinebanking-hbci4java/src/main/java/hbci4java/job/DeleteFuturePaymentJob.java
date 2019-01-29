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

import domain.FuturePayment;
import domain.AbstractScaTransaction;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVTermUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

/**
 * Only for future payment (GVTermUebSEPA)
 */
public class DeleteFuturePaymentJob extends ScaRequiredJob {

    private String jobName;

    @Override
    protected AbstractSEPAGV createSepaJob(AbstractScaTransaction sepaTransaction, PinTanPassport passport, String sepaPain) {
        FuturePayment singlePayment = (FuturePayment) sepaTransaction;

        Konto src = getOrderAccount(sepaTransaction, passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        jobName = GVTermUebSEPADel.getLowlevelName();

        GVTermUebSEPADel sepadelgv = new GVTermUebSEPADel(passport, jobName, sepaPain);

        sepadelgv.setParam("orderid", singlePayment.getOrderId());
        sepadelgv.setParam("date", singlePayment.getExecutionDate().toString());

        sepadelgv.setParam("src", src);
        sepadelgv.setParam("dst", dst);
        sepadelgv.setParam("btg", new Value(singlePayment.getAmount()));
        sepadelgv.setParam("usage", singlePayment.getPurpose());

        sepadelgv.verifyConstraints();

        return sepadelgv;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return jobName;
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
