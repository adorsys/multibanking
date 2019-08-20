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
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTermMultiUebSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.util.Collections;
import java.util.List;

/**
 * Only for future payment (GVTermUebSEPA)
 */
@RequiredArgsConstructor
public class DeleteFutureBulkPaymentJob extends ScaRequiredJob<AuthorisationCodeResponse> {

    private final TransactionRequest transactionRequest;
    private String jobName;

    @Override
    public List<AbstractHBCIJob> createHbciJobs(PinTanPassport passport) {
        FutureBulkPayment futureBulkPayment = (FutureBulkPayment) transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        jobName = GVTermMultiUebSEPADel.getLowlevelName();

        GVTermMultiUebSEPADel sepadelgv = new GVTermMultiUebSEPADel(passport, jobName);

        sepadelgv.setParam("orderid", futureBulkPayment.getOrderId());
        sepadelgv.setParam("src", src);
        sepadelgv.verifyConstraints();

        return Collections.singletonList(sepadelgv);
    }

    @Override
    AuthorisationCodeResponse createJobResponse(PinTanPassport passport, AuthorisationCodeResponse response) {
        return response;
    }

    @Override
    TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return jobName;
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
