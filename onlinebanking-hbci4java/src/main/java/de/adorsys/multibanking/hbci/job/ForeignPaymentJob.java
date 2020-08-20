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
import de.adorsys.multibanking.domain.transaction.ForeignPayment;
import org.kapott.hbci.GV.GVDTAZV;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;

public class ForeignPaymentJob extends AbstractPaymentJob<ForeignPayment, GVDTAZV> {

    public ForeignPaymentJob(TransactionRequest<ForeignPayment> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    GVDTAZV createHbciJob() {
        Konto src = getHbciKonto();

        GVDTAZV hbciJob = new GVDTAZV(dialog.getPassport(), GVDTAZV.getLowlevelName());

        hbciJob.setParam("src", src);
        hbciJob.setParam("dtazv", "B" + transactionRequest.getTransaction().getRawRequestData());
        hbciJob.verifyConstraints();

        return hbciJob;
    }

    @Override
    protected String getHbciJobName() {
        return GVDTAZV.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
