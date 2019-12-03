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
import de.adorsys.multibanking.domain.transaction.StandingOrderRequest;
import de.adorsys.multibanking.hbci.model.HbciCycleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV_Result.GVRPayment;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@RequiredArgsConstructor
@Slf4j
public class NewStandingOrderJob extends AbstractPaymentJob<StandingOrderRequest> {

    private final TransactionRequest<StandingOrderRequest> transactionRequest;
    private GVDauerSEPANew hbciNewStandingOrderJob;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        StandingOrderRequest standingOrder = transactionRequest.getTransaction();

        Konto src = getPsuKonto(passport);

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        hbciNewStandingOrderJob = new GVDauerSEPANew(passport);

        hbciNewStandingOrderJob.setParam("src", src);
        hbciNewStandingOrderJob.setParam("dst", dst);
        hbciNewStandingOrderJob.setParam("btg", new Value(standingOrder.getAmount(), standingOrder.getCurrency()));
        hbciNewStandingOrderJob.setParam("usage", standingOrder.getUsage());

        // standing order specific parameter
        if (standingOrder.getFirstExecutionDate() != null) {
            hbciNewStandingOrderJob.setParam("firstdate", standingOrder.getFirstExecutionDate().toString());
        }
        if (standingOrder.getCycle() != null) {
            hbciNewStandingOrderJob.setParam("timeunit", HbciCycleMapper.cycleToTimeunit(standingOrder.getCycle()));
            // M
            // month, W
            // week
            hbciNewStandingOrderJob.setParam("turnus", HbciCycleMapper.cycleToTurnus(standingOrder.getCycle())); //
            // 1W = every
            // week, 2M = every two months
        }
        hbciNewStandingOrderJob.setParam("execday", standingOrder.getExecutionDay()); // W: 1-7, M: 1-31
        if (standingOrder.getLastExecutionDate() != null) {
            hbciNewStandingOrderJob.setParam("lastdate", standingOrder.getLastExecutionDate().toString());
        }
        if (standingOrder.getPurposecode() != null) {
            hbciNewStandingOrderJob.setParam("purposecode", standingOrder.getPurposecode());
        }

        hbciNewStandingOrderJob.verifyConstraints();

        return hbciNewStandingOrderJob;
    }

    @Override
    AbstractHBCIJob getHbciJob() {
        return hbciNewStandingOrderJob;
    }

    @Override
    TransactionRequest<StandingOrderRequest> getTransactionRequest() {
        return transactionRequest;
    }

    @Override
    protected String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return GVDauerSEPANew.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return ((GVRPayment) paymentGV).getOrderId();
    }
}
