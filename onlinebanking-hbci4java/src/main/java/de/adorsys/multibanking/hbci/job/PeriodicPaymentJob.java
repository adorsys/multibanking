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
import de.adorsys.multibanking.domain.transaction.PeriodicPayment;
import de.adorsys.multibanking.hbci.HbciBpdCacheHolder;
import de.adorsys.multibanking.hbci.model.HbciCycleMapper;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@Slf4j
public class PeriodicPaymentJob extends AbstractPaymentJob<PeriodicPayment> {

    public PeriodicPaymentJob(TransactionRequest<PeriodicPayment> transactionRequest, HbciBpdCacheHolder bpdCacheHolder) {
        super(transactionRequest, bpdCacheHolder);
    }

    @Override
    GVDauerSEPANew createHbciJob() {
        PeriodicPayment standingOrder = transactionRequest.getTransaction();

        Konto src = getHbciKonto();

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        GVDauerSEPANew hbciJob = new GVDauerSEPANew(dialog.getPassport(), getSepaVersion());

        hbciJob.setParam("src", src);
        hbciJob.setParam("dst", dst);
        hbciJob.setParam("btg", new Value(standingOrder.getAmount(), standingOrder.getCurrency()));
        hbciJob.setParam("usage", standingOrder.getUsage());

        // standing order specific parameter
        if (standingOrder.getFirstExecutionDate() != null) {
            hbciJob.setParam("firstdate", standingOrder.getFirstExecutionDate().toString());
        }
        if (standingOrder.getCycle() != null) {
            hbciJob.setParam("timeunit", HbciCycleMapper.cycleToTimeunit(standingOrder.getCycle()));
            // M
            // month, W
            // week
            hbciJob.setParam("turnus", HbciCycleMapper.cycleToTurnus(standingOrder.getCycle())); //
            // 1W = every
            // week, 2M = every two months
        }
        hbciJob.setParam("execday", standingOrder.getExecutionDay()); // W: 1-7, M: 1-31
        if (standingOrder.getLastExecutionDate() != null) {
            hbciJob.setParam("lastdate", standingOrder.getLastExecutionDate().toString());
        }
        if (standingOrder.getPurposecode() != null) {
            hbciJob.setParam("purposecode", standingOrder.getPurposecode());
        }
        if (standingOrder.getEndToEndId() != null) {
            hbciJob.setParam("endtoendid", standingOrder.getEndToEndId());
        }

        hbciJob.verifyConstraints();

        return hbciJob;
    }

    @Override
    protected String getHbciJobName() {
        return GVDauerSEPANew.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return ((GVRPayment) paymentGV).getOrderId();
    }
}
