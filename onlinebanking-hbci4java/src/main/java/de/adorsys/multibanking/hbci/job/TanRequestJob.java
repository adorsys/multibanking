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
import lombok.RequiredArgsConstructor;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class TanRequestJob extends ScaRequiredJob {

    private final TransactionRequest transactionRequest;

    @Override
    TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return null;
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public AbstractHBCIJob createScaMessage(PinTanPassport passport) {
        return null;
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        return Collections.emptyList();
    }

    @Override
    EmptyResponse createJobResponse(PinTanPassport passport, AbstractHBCIJob hbciJob) {
        return new EmptyResponse();
    }
}
