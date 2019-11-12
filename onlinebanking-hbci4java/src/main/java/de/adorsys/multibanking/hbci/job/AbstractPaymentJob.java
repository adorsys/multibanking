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

import de.adorsys.multibanking.domain.response.PaymentResponse;
import de.adorsys.multibanking.domain.transaction.AbstractPayment;
import de.adorsys.multibanking.hbci.model.HbciTanSubmit;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIMsgStatus;

import java.util.List;
import java.util.Optional;

abstract class AbstractPaymentJob<T extends AbstractPayment> extends ScaAwareJob<T, PaymentResponse> {

    @Override
    PaymentResponse createJobResponse(PinTanPassport passport, HbciTanSubmit tanSubmit,
                                      List<HBCIMsgStatus> msgStatusList) {
        String transactionId = Optional.ofNullable(getTransactionId())
            .orElseGet(tanSubmit::getOrderRef);
        PaymentResponse paymentResponse = new PaymentResponse(transactionId);
        paymentResponse.setWarnings(collectWarnings(msgStatusList));
        return paymentResponse;
    }

    private String getTransactionId() {
        return Optional.ofNullable(getHbciJob())
            .map(abstractHBCIJob -> orderIdFromJobResult(abstractHBCIJob.getJobResult()))
            .orElse(null);
    }

    abstract AbstractHBCIJob getHbciJob();

    public abstract String orderIdFromJobResult(HBCIJobResult paymentGV);
}
