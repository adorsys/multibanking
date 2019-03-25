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
import de.adorsys.multibanking.domain.RawSepaPayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV.GVRawSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

public class RawSepaJob extends ScaRequiredJob {

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return GVRawSEPA.getLowlevelName();
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport, String rawData) {
        RawSepaPayment sepaPayment = (RawSepaPayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        String jobName;
        switch (sepaPayment.getSepaTransactionType()) {
            case SINGLE_PAYMENT:
                jobName = GVUebSEPA.getLowlevelName();
                break;
            case BULK_PAYMENT:
                jobName = "SammelUebSEPA";
                break;
            case STANDING_ORDER:
                jobName = GVDauerSEPANew.getLowlevelName();
                break;
            default:
                throw new IllegalArgumentException("unsupported raw sepa transaction: " + transaction.getTransactionType());
        }

        GVRawSEPA sepagv = new GVRawSEPA(passport, jobName, sepaPayment.getRawData());
        sepagv.setParam("src", src);

        sepagv.verifyConstraints();

        return sepagv;
    }

    public enum PaymentType {
        SINGLE, FUTURE, BULK, PERIODIC
    }
}
