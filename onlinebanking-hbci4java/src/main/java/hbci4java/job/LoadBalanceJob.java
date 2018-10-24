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

import domain.*;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciMapping;
import hbci4java.model.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVKUmsAll;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hbci4java.job.AccountInformationJob.extractTanTransportTypes;
import static hbci4java.model.HbciDialogFactory.createDialog;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

@Slf4j
public class LoadBalanceJob {

    public static BankAccountBalance loadBalance(LoadBalanceRequest loadBalanceRequest) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(loadBalanceRequest.getBankCode() != null ? loadBalanceRequest.getBankCode() :
                        loadBalanceRequest.getBankAccess().getBankCode())
                .customerId(loadBalanceRequest.getBankAccess().getBankLogin())
                .login(loadBalanceRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(loadBalanceRequest.getBankAccess().getHbciPassportState())
                .pin(loadBalanceRequest.getPin())
                .build();
        dialogRequest.setBpd(loadBalanceRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        Konto account = createAccount(dialog, loadBalanceRequest.getBankAccount());

        AbstractHBCIJob balanceJob = createBalanceJob(dialog, account);

        // Let the Handler execute all jobs in one batch
        HBCIExecStatus status = dialog.execute(true);
        if (!status.isOK()) {
            log.error("Status of SaldoReq+KUmsAll+DauerSEPAList batch job not OK " + status);

            if (initFailed(status)) {
                throw new HBCI_Exception(status.getErrorString());
            }
        }

        if (balanceJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new HBCI_Exception(balanceJob.getJobResult().getJobStatus().getErrorString());
        }

        return HbciMapping.createBalance((GVRSaldoReq) balanceJob.getJobResult());
    }

    private static AbstractHBCIJob createBalanceJob(HBCIDialog dialog, Konto account) {
        AbstractHBCIJob balanceJob = newJob("SaldoReq", dialog.getPassport());
        balanceJob.setParam("my", account);
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
