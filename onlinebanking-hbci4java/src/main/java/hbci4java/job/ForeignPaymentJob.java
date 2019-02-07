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
import domain.ForeignPayment;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVDTAZV;
import org.kapott.hbci.GV.GVTermUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.GVRTermUeb;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

public class ForeignPaymentJob extends ScaRequiredJob {

    @Override
    protected AbstractHBCIJob createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                            String rawData) {
        Konto src = getDebtorAccount(transaction, passport);

        GVDTAZV gv = new GVDTAZV(passport, GVDTAZV.getLowlevelName());

        gv.setParam("src", src);
        gv.setParam("dtazv", "B"+transaction.getRawData());
        gv.verifyConstraints();

        return gv;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return GVDTAZV.getLowlevelName();
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
