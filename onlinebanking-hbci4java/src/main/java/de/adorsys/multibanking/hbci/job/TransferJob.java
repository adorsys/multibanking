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

import de.adorsys.multibanking.domain.exception.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import de.adorsys.multibanking.hbci.model.HbciDialogRequest;
import de.adorsys.multibanking.hbci.model.HbciObjectMapper;
import de.adorsys.multibanking.hbci.model.HbciObjectMapperImpl;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUmbSEPA;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;
import static de.adorsys.multibanking.hbci.model.HbciDialogFactory.createDialog;
import static de.adorsys.multibanking.hbci.model.HbciDialogType.JOBS;

@Slf4j
public class TransferJob {

    private HbciObjectMapper hbciObjectMapper = new HbciObjectMapperImpl();

    public void requestTransfer(TransactionRequest sepaTransactionRequest) {
        HbciDialogRequest dialogRequest = hbciObjectMapper.toHbciDialogRequest(sepaTransactionRequest, null);

        HBCIJobsDialog dialog = (HBCIJobsDialog)createDialog(JOBS, null, dialogRequest, null);

        AbstractHBCIJob hbciJob = createHbciJob(sepaTransactionRequest.getTransaction(), dialog.getPassport(), null);

        dialog.addTask(hbciJob);

        // Let the Handler submitAuthorizationCode all jobs in one batch
        HBCIExecStatus dialogStatus = dialog.execute(false);
        if (!dialogStatus.isOK()) {
            log.warn(dialogStatus.getErrorMessages().toString());
        }

        if (hbciJob.getJobResult().getJobStatus().hasErrors()) {
            throw new MultibankingException(HBCI_ERROR, hbciJob.getJobResult().getJobStatus().getErrorList().stream()
                .map(messageString -> Message.builder().renderedMessage(messageString).build())
                .collect(Collectors.toList()));
        }

        dialog.execute(true);
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
        if (singlePayment.getPurpose() != null) {
            sepagv.setParam("usage", singlePayment.getPurpose());
        }

        sepagv.verifyConstraints();

        return sepagv;
    }

    private Konto getDebtorAccount(AbstractScaTransaction sepaTransaction, PinTanPassport passport) {
        return Optional.ofNullable(sepaTransaction.getPsuAccount())
            .map(bankAccount -> {
                Konto konto = passport.findAccountByAccountNumber(bankAccount.getAccountNumber());
                konto.iban = bankAccount.getIban();
                konto.bic = bankAccount.getBic();
                return konto;
            })
            .orElse(null);
    }
}
