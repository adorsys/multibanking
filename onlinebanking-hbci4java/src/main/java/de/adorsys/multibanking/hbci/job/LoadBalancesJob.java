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

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.LoadBalancesResponse;
import de.adorsys.multibanking.domain.transaction.LoadBalances;
import de.adorsys.multibanking.hbci.util.HbciErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVSaldoReq;
import org.kapott.hbci.GV_Result.GVRSaldoReq;

@Slf4j
public class LoadBalancesJob extends ScaAwareJob<LoadBalances, LoadBalancesResponse> {

    public LoadBalancesJob(TransactionRequest<LoadBalances> transactionRequest) {
        super(transactionRequest);
    }

    @Override
    GVSaldoReq createHbciJob() {
        GVSaldoReq hbciJob = new GVSaldoReq(dialog.getPassport());
        hbciJob.setParam("my", getHbciKonto());
        return hbciJob;
    }

    @Override
    String getHbciJobName() {
        return GVSaldoReq.getLowlevelName();
    }

    @Override
    public LoadBalancesResponse createJobResponse() {
        if (getOrCreateHbciJob().getJobResult().getJobStatus().hasErrors()) {
            log.error("Balance job not OK");
            throw HbciErrorUtils.toMultibankingException(getOrCreateHbciJob().getJobResult().getJobStatus());
        }

        BankAccount bankAccount = transactionRequest.getTransaction().getPsuAccount();

        GVRSaldoReq jobResult = (GVRSaldoReq) getOrCreateHbciJob().getJobResult();
        if (jobResult.getEntries() != null && !jobResult.getEntries().isEmpty()) {
            bankAccount.setBalances(accountStatementMapper.createBalancesReport((GVRSaldoReq) getOrCreateHbciJob().getJobResult(),
                bankAccount.getAccountNumber()));
        }

        return new LoadBalancesResponse(bankAccount);
    }
}
