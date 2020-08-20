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
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVUmbSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@Slf4j
public class TransferJob extends AbstractPaymentJob<SinglePayment, GVUmbSEPA> {

    public TransferJob(TransactionRequest<SinglePayment> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    GVUmbSEPA createHbciJob() {
        SinglePayment singlePayment = transactionRequest.getTransaction();

        Konto src = getHbciKonto();

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic() != null ? singlePayment.getReceiverBic() : src.bic; //internal transfer, same bic

        GVUmbSEPA hbciJob = new GVUmbSEPA(dialog.getPassport(), GVUmbSEPA.getLowlevelName(), null);

        hbciJob.setParam("src", src);
        hbciJob.setParam("dst", dst);
        hbciJob.setParam("btg", new Value(singlePayment.getAmount(), singlePayment.getCurrency()));
        if (singlePayment.getPurpose() != null) {
            hbciJob.setParam("usage", singlePayment.getPurpose());
        }
        if (singlePayment.getEndToEndId() != null) {
            hbciJob.setParam("endtoendid", singlePayment.getEndToEndId());
        }

        hbciJob.verifyConstraints();

        return hbciJob;
    }

    @Override
    String getHbciJobName() {
        return GVUmbSEPA.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }
}
