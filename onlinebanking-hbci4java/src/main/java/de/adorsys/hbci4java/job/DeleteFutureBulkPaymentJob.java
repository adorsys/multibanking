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

package de.adorsys.hbci4java.job;

import domain.AbstractScaTransaction;
import domain.FutureBulkPayment;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTermMultiUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

/**
 * Only for future payment (GVTermUebSEPA)
 */
public class DeleteFutureBulkPaymentJob extends ScaRequiredJob {

    private String jobName;

    @Override
    protected AbstractHBCIJob createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                            String rawData) {
        FutureBulkPayment futureBulkPayment = (FutureBulkPayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        jobName = GVTermMultiUebSEPADel.getLowlevelName();

        GVTermMultiUebSEPADel sepadelgv = new GVTermMultiUebSEPADel(passport, jobName);

        sepadelgv.setParam("orderid", futureBulkPayment.getOrderId());
        sepadelgv.setParam("src", src);
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
