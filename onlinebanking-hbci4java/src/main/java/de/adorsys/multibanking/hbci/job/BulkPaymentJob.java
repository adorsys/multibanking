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
import de.adorsys.multibanking.domain.transaction.BulkPayment;
import de.adorsys.multibanking.domain.transaction.FutureBulkPayment;
import de.adorsys.multibanking.domain.transaction.SinglePayment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVMultiUebSEPA;
import org.kapott.hbci.GV.GVTermMultiUebSEPA;
import org.kapott.hbci.GV_Result.GVRTermUeb;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class BulkPaymentJob extends ScaRequiredJob<EmptyResponse> {

    private final TransactionRequest transactionRequest;

    @Override
    public AbstractHBCIJob createScaMessage(PinTanPassport passport) {
        BulkPayment bulkPayment = (BulkPayment) transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        AbstractSEPAGV sepagv;
        if (bulkPayment instanceof FutureBulkPayment) {
            sepagv = new GVTermMultiUebSEPA(passport, GVTermMultiUebSEPA.getLowlevelName());
            sepagv.setParam("date", ((FutureBulkPayment) bulkPayment).getExecutionDate().toString());
        } else {
            sepagv = new GVMultiUebSEPA(passport, GVMultiUebSEPA.getLowlevelName());
        }

        sepagv.setParam("src", src);
        sepagv.setParam("batchbook", BooleanUtils.isTrue(bulkPayment.getBatchbooking()) ? "1" : "0");

        for (int i = 0; i < bulkPayment.getPayments().size(); i++) {
            SinglePayment payment = bulkPayment.getPayments().get(i);

            Konto dst = new Konto();
            dst.name = payment.getReceiver();
            dst.iban = payment.getReceiverIban();
            dst.bic = payment.getReceiverBic();

            sepagv.setParam("dst", i, dst);
            sepagv.setParam("btg", i, new Value(payment.getAmount(), payment.getCurrency()));
            if (payment.getPurpose() != null) {
                sepagv.setParam("usage", i, payment.getPurpose());
            }
            if (payment.getPurposecode() != null) {
                sepagv.setParam("purposecode", i, payment.getPurposecode());
            }
        }

        sepagv.verifyConstraints();

        return sepagv;
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        return Collections.emptyList();
    }

    @Override
    EmptyResponse createJobResponse(PinTanPassport passport) {
        return new EmptyResponse();
    }

    @Override
    TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        if (transactionType == AbstractScaTransaction.TransactionType.FUTURE_BULK_PAYMENT) {
            return "TermMultiUebSEPA";
        }
        return "MultiUebSEPA";
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return paymentGV instanceof GVRTermUeb ? ((GVRTermUeb) paymentGV).getOrderId() : null;
    }
}
