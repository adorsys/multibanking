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
import de.adorsys.multibanking.domain.Product;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadBalanceRequest;
import de.adorsys.multibanking.hbci.model.HbciDialogFactory;
import de.adorsys.multibanking.hbci.model.HbciDialogRequest;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSaldoReq;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.*;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoadBalanceJob {

    public static List<BankAccount> loadBalances(LoadBalanceRequest loadBalanceRequest) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
            .bankCode(loadBalanceRequest.getBankCode() != null ? loadBalanceRequest.getBankCode() :
                loadBalanceRequest.getBankAccess().getBankCode())
            .customerId(loadBalanceRequest.getBankAccess().getBankLogin())
            .login(loadBalanceRequest.getBankAccess().getBankLogin2())
            .hbciPassportState(loadBalanceRequest.getBankAccess().getHbciPassportState())
            .pin(loadBalanceRequest.getPin())
            .build();

        dialogRequest.setProduct(Optional.ofNullable(loadBalanceRequest.getProduct())
            .map(product -> new Product(product.getName(), product.getVersion()))
            .orElse(null));
        dialogRequest.setBpd(loadBalanceRequest.getBpd());

        HBCIDialog dialog = HbciDialogFactory.startHbciDialog(null, dialogRequest);

        Map<AbstractHBCIJob, BankAccount> jobs = new HashMap<>();

        loadBalanceRequest.getBankAccounts().forEach(bankAccount -> {
            Konto account = createAccount(dialog, bankAccount);
            jobs.put(createBalanceJob(dialog, account), bankAccount);
        });

        HBCIExecStatus status = dialog.execute(true);
        if (!status.isOK()) {
            log.error("Status of balance job not OK " + status);

            if (initFailed(status)) {
                throw new MultibankingException(HBCI_ERROR, status.getDialogStatus().getErrorMessages());
            }
        }

        List<BankAccount> bankAccounts = new ArrayList<>();
        jobs.keySet().forEach(job -> {
            if (job.getJobResult().getJobStatus().hasErrors()) {
                log.error("Balance job not OK");
                throw new MultibankingException(HBCI_ERROR, job.getJobResult().getJobStatus().getErrorList());
            }
            BankAccount bankAccount = jobs.get(job);
            bankAccount.setBalances(HbciMapping.createBalance((GVRSaldoReq) job.getJobResult(),
                bankAccount.getAccountNumber()));
            bankAccounts.add(bankAccount);
        });
        return bankAccounts;
    }

    private static AbstractHBCIJob createBalanceJob(HBCIDialog dialog, Konto konto) {
        AbstractHBCIJob balanceJob = new GVSaldoReq(dialog.getPassport());
        balanceJob.setParam("my", konto);
        dialog.addTask(balanceJob);
        return balanceJob;
    }

    private static Konto createAccount(HBCIDialog dialog, BankAccount bankAccount) {
        Konto account = dialog.getPassport().findAccountByAccountNumber(bankAccount.getAccountNumber());
        account.iban = bankAccount.getIban();
        account.bic = bankAccount.getBic();
        account.curr = bankAccount.getCurrency();
        account.country = bankAccount.getCountry();
        return account;
    }

    private static boolean initFailed(HBCIExecStatus status) {
        return status.getErrorMessages().stream()
            .anyMatch(line -> line.charAt(0) == '9');
    }
}
