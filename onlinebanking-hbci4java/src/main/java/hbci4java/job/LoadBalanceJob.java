/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package hbci4java.job;

import domain.BankAccount;
import domain.HBCIProduct;
import domain.request.LoadBalanceRequest;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSaldoReq;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.*;
import java.util.stream.Stream;

import static hbci4java.model.HbciDialogFactory.createDialog;

@Slf4j
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

        dialogRequest.setHbciProduct(Optional.ofNullable(loadBalanceRequest.getHbciProduct())
                .map(product -> new HBCIProduct(product.getProduct(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(loadBalanceRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        Map<AbstractHBCIJob, BankAccount> jobs = new HashMap<>();

        loadBalanceRequest.getBankAccounts().stream().forEach( bankAccount -> {
            Konto account = createAccount(dialog, bankAccount);
            jobs.put(createBalanceJob(dialog, account), bankAccount);
        });

        // Let the Handler execute all jobs in one batch
        HBCIExecStatus status = dialog.execute(true);
        if (!status.isOK()) {
            log.error("Status of balance job not OK " + status);

            if (initFailed(status)) {
                throw new HBCI_Exception(status.getErrorString());
            }
        }

        List<BankAccount> bankAccounts = new ArrayList<>();
        jobs.keySet().stream().forEach( job -> {
            if (job.getJobResult().getJobStatus().hasErrors()) {
                log.error("Balance job not OK");
                throw new HBCI_Exception(job.getJobResult().getJobStatus().getErrorString());
            }
            BankAccount bankAccount = jobs.get(job);
            bankAccount.setBalances(HbciMapping.createBalance((GVRSaldoReq) job.getJobResult()));
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
        return account;
    }

    private static boolean initFailed(HBCIExecStatus status) {
        return Stream.of(StringUtils.split(status.getErrorString(), System.getProperty("line.separator")))
                .anyMatch(line -> line.charAt(0) == '9');
    }
}
