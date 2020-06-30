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
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.PeriodicPayment;
import de.adorsys.multibanking.hbci.model.HbciCycleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVDauerSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@RequiredArgsConstructor
@Slf4j
public class DeleteStandingOrderJob extends AbstractPaymentJob<PeriodicPayment> {

    private final TransactionRequest<PeriodicPayment> transactionRequest;
    private GVDauerSEPADel hbciDeleteStandingOrderJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        PeriodicPayment standingOrder = transactionRequest.getTransaction();

        Konto src = getHbciKonto(passport);

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        hbciDeleteStandingOrderJob = new GVDauerSEPADel(passport);

        hbciDeleteStandingOrderJob.setParam("src", src);
        hbciDeleteStandingOrderJob.setParam("dst", dst);
        hbciDeleteStandingOrderJob.setParam("btg", new Value(standingOrder.getAmount(), standingOrder.getCurrency()));
        hbciDeleteStandingOrderJob.setParam("usage", standingOrder.getUsage());

        hbciDeleteStandingOrderJob.setParam("orderid", standingOrder.getOrderId());

        // standing order specific parameter
        if (standingOrder.getFirstExecutionDate() != null) {
            hbciDeleteStandingOrderJob.setParam("firstdate", standingOrder.getFirstExecutionDate().toString());
        }
        if (standingOrder.getCycle() != null) {
            hbciDeleteStandingOrderJob.setParam("timeunit",
                HbciCycleMapper.cycleToTimeunit(standingOrder.getCycle())); // M
            // month, W
            // week
            hbciDeleteStandingOrderJob.setParam("turnus", HbciCycleMapper.cycleToTurnus(standingOrder.getCycle()));
            // 1W = every
            // week, 2M = every two months
        }
        hbciDeleteStandingOrderJob.setParam("execday", standingOrder.getExecutionDay()); // W: 1-7, M: 1-31
        if (standingOrder.getLastExecutionDate() != null) {
            hbciDeleteStandingOrderJob.setParam("lastdate", standingOrder.getLastExecutionDate().toString());
        }

        hbciDeleteStandingOrderJob.verifyConstraints();

        return hbciDeleteStandingOrderJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return hbciDeleteStandingOrderJob;
    }

    @Override
    TransactionRequest<PeriodicPayment> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return GVDauerSEPADel.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }

}
