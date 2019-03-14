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

package de.adorsys.hbci4java.job;

import de.adorsys.hbci4java.model.HbciMapping;
import domain.AbstractScaTransaction;
import domain.StandingOrder;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV_Result.GVRDauerNew;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@Slf4j
public class NewStandingOrderJob extends ScaRequiredJob {

    @Override
    protected AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport, String rawData) {
        StandingOrder standingOrder = (StandingOrder) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        GVDauerSEPANew gvDauerSEPANew = new GVDauerSEPANew(passport, rawData);

        gvDauerSEPANew.setParam("src", src);
        gvDauerSEPANew.setParam("dst", dst);
        gvDauerSEPANew.setParam("btg", new Value(standingOrder.getAmount()));
        gvDauerSEPANew.setParam("usage", standingOrder.getUsage());


        // standing order specific parameter
        if (standingOrder.getFirstExecutionDate() != null) {
            gvDauerSEPANew.setParam("firstdate", standingOrder.getFirstExecutionDate().toString());
        }
        if (standingOrder.getCycle() != null) {
            gvDauerSEPANew.setParam("timeunit", HbciMapping.cycleToTimeunit(standingOrder.getCycle())); // M month, W week
            gvDauerSEPANew.setParam("turnus", HbciMapping.cycleToTurnus(standingOrder.getCycle())); // 1W = every week, 2M = every two months
        }
        gvDauerSEPANew.setParam("execday", standingOrder.getExecutionDay()); // W: 1-7, M: 1-31
        if (standingOrder.getLastExecutionDate() != null) {
            gvDauerSEPANew.setParam("lastdate", standingOrder.getLastExecutionDate().toString());
        }

        gvDauerSEPANew.verifyConstraints();

        return gvDauerSEPANew;
    }

    @Override
    protected String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return GVDauerSEPANew.getLowlevelName();
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return ((GVRDauerNew) paymentGV).getOrderId();
    }
}
