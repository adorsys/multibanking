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

import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.Product;
import de.adorsys.multibanking.domain.SinglePayment;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.hbci.model.HbciDialogRequest;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUmbSEPA;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.Optional;

import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.createDialog;

/**
 * Created by cbr on 28.02.19.
 */
@Slf4j
public class TransferJob {
    public void requestTransfer(TransactionRequest sepaTransactionRequest) {
        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(sepaTransactionRequest.getBankCode() != null ? sepaTransactionRequest.getBankCode() :
                        sepaTransactionRequest.getBankAccess().getBankCode())
                .customerId(sepaTransactionRequest.getBankAccess().getBankLogin())
                .login(sepaTransactionRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(sepaTransactionRequest.getBankAccess().getHbciPassportState())
                .pin(sepaTransactionRequest.getPin())
                .build();

        dialogRequest.setProduct(Optional.ofNullable(sepaTransactionRequest.getProduct())
                .map(product -> new Product(product.getName(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(sepaTransactionRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        AbstractHBCIJob hbciJob = createHbciJob(sepaTransactionRequest.getTransaction(), dialog.getPassport(), null);

        dialog.addTask(hbciJob);

        // Let the Handler submitAuthorizationCode all jobs in one batch
        HBCIExecStatus dialogStatus = dialog.execute(true);
        if (!dialogStatus.isOK()) {
            log.warn(dialogStatus.getErrorString());
        }

        if (hbciJob.getJobResult().getJobStatus().hasErrors()) {
            log.error("Bookings job not OK");
            throw new HBCI_Exception(hbciJob.getJobResult().getJobStatus().getErrorString());
        }
    }

    private AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport,
                                         String rawData) {
        SinglePayment singlePayment = (SinglePayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        AbstractSEPAGV sepagv = new GVUmbSEPA(passport, GVUmbSEPA.getLowlevelName(), rawData);

        sepagv.setParam("src", src);
        sepagv.setParam("dst", dst);
        sepagv.setParam("btg", new Value(singlePayment.getAmount(), singlePayment.getCurrency()));
        sepagv.setParam("usage", singlePayment.getPurpose());

        sepagv.verifyConstraints();

        return sepagv;
    }

    Konto getDebtorAccount(AbstractScaTransaction sepaTransaction, PinTanPassport passport) {
        return Optional.ofNullable(sepaTransaction.getDebtorBankAccount())
                .map(bankAccount -> {
                    Konto konto = passport.findAccountByAccountNumber(bankAccount.getAccountNumber());
                    konto.iban = bankAccount.getIban();
                    konto.bic = bankAccount.getBic();
                    return konto;
                })
                .orElse(null);
    }
}
