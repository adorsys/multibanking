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
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadBalanceRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.LoadBalancesResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSaldoReq;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class LoadBalancesJob extends ScaRequiredJob<LoadBalancesResponse> {

    private final LoadBalanceRequest loadBalanceRequest;

    private Map<AbstractHBCIJob, BankAccount> jobs;

    private static AbstractHBCIJob createBalanceJob(PinTanPassport passport, Konto konto) {
        AbstractHBCIJob balanceJob = new GVSaldoReq(passport);
        balanceJob.setParam("my", konto);
        return balanceJob;
    }

    private static Konto createAccount(BankAccount bankAccount) {
        Konto account = new Konto();
        account.bic = bankAccount.getBic();
        account.number = bankAccount.getAccountNumber();
        account.iban = bankAccount.getIban();
        account.blz = bankAccount.getBlz();
        account.curr = bankAccount.getCurrency();
        account.country = bankAccount.getCountry();
        return account;
    }

    private static boolean initFailed(HBCIExecStatus status) {
        return status.getErrorMessages().stream()
            .anyMatch(line -> line.charAt(0) == '9');
    }

    @Override
    public List<AbstractHBCIJob> createHbciJobs(PinTanPassport passport) {
        jobs = new HashMap<>();

        loadBalanceRequest.getBankAccounts().forEach(bankAccount -> {
            Konto account = createAccount(bankAccount);
            jobs.put(createBalanceJob(passport, account), bankAccount);
        });

        return new ArrayList<>(jobs.keySet());
    }

    @Override
    protected void execute(HBCIDialog dialog, boolean closeDialog) {
        HBCIExecStatus status = dialog.execute(closeDialog);
        if (!status.isOK()) {
            log.error("Status of balance job not OK " + status);

            if (initFailed(status)) {
                throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages());
            }
        }
    }

    @Override
    BankAccount getPsuBankAccount() {
        return loadBalanceRequest.getBankAccounts().get(0);
    }

    @Override
    TransactionRequest getTransactionRequest() {
        return loadBalanceRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return GVSaldoReq.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public LoadBalancesResponse createJobResponse(PinTanPassport passport, AuthorisationCodeResponse response) {
        //TODO check for needed 2FA
        List<BankAccount> bankAccounts = jobs.keySet().stream()
            .map(job -> {
                if (job.getJobResult().getJobStatus().hasErrors()) {
                    log.error("Balance job not OK");
                    throw new MultibankingException(HBCI_ERROR, job.getJobResult().getJobStatus().getErrorList());
                }
                BankAccount bankAccount = jobs.get(job);
                bankAccount.setBalances(HbciMapping.createBalance((GVRSaldoReq) job.getJobResult(),
                    bankAccount.getAccountNumber()));
                return bankAccount;
            })
            .collect(Collectors.toList());

        return new LoadBalancesResponse(bankAccounts);
    }
}
