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
import de.adorsys.multibanking.domain.response.PaymentStatusResponse;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.PaymentStatusReqest;
import de.adorsys.multibanking.hbci.model.HbciTanSubmit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVInstanstUebSEPAStatus;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIMsgStatus;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class InstantPaymentStatusJob extends ScaAwareJob<PaymentStatusReqest, PaymentStatusResponse> {

    private final TransactionRequest<PaymentStatusReqest> paymentStatusReqest;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        GVInstanstUebSEPAStatus paymentStatusHbciJob = new GVInstanstUebSEPAStatus(passport);
        paymentStatusHbciJob.setParam("my", getPsuKonto(passport));
        paymentStatusHbciJob.setParam("orderid", paymentStatusReqest.getTransaction().getPaymentId());
        return paymentStatusHbciJob;
    }

    @Override
    TransactionRequest<PaymentStatusReqest> getTransactionRequest() {
        return paymentStatusReqest;
    }

    @Override
    String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return GVInstanstUebSEPAStatus.getLowlevelName();
    }

    @Override
    public PaymentStatusResponse createJobResponse(PinTanPassport passport, HbciTanSubmit tanSubmit,
                                                   List<HBCIMsgStatus> msgStatusList) {
        return null;
    }
}
