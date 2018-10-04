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

import domain.AbstractPayment;
import domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class SinglePaymentJob extends AbstractPaymentJob {

    @Override
    protected AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain) {
        SinglePayment singlePayment = (SinglePayment) payment;

        Konto src = passport.findAccountByAccountNumber(payment.getSenderAccountNumber());
        src.iban = payment.getSenderIban();
        src.bic = payment.getSenderBic();

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        GVUebSEPA uebSEPA = new GVUebSEPA(passport, GVUebSEPA.getLowlevelName(), sepaPain);
        uebSEPA.setParam("src", src);
        uebSEPA.setParam("dst", dst);
        uebSEPA.setParam("btg", new Value(singlePayment.getAmount()));
        uebSEPA.setParam("usage", singlePayment.getPurpose());

        uebSEPA.verifyConstraints();

        return uebSEPA;
    }

    @Override
    protected String getJobName() {
        return GVUebSEPA.getLowlevelName();
    }
}
