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
import de.adorsys.multibanking.domain.response.EmptyResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUmbSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class TransferJob extends ScaRequiredJob<SinglePayment, EmptyResponse> {

    private final TransactionRequest<SinglePayment> transactionRequest;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        SinglePayment singlePayment = transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        AbstractSEPAGV sepagv = new GVUmbSEPA(passport, GVUmbSEPA.getLowlevelName(), null);

        sepagv.setParam("src", src);
        sepagv.setParam("dst", dst);
        sepagv.setParam("btg", new Value(singlePayment.getAmount(), singlePayment.getCurrency()));
        if (singlePayment.getPurpose() != null) {
            sepagv.setParam("usage", singlePayment.getPurpose());
        }

        sepagv.verifyConstraints();

        return sepagv;
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        return Collections.emptyList();
    }

    @Override
    TransactionRequest<SinglePayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return GVUmbSEPA.getLowlevelName();
    }

    @Override
    EmptyResponse createJobResponse(PinTanPassport passport) {
        return new EmptyResponse();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }
}
