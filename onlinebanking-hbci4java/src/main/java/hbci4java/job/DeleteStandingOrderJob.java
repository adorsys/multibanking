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

import domain.SepaTransaction;
import domain.StandingOrder;
import hbci4java.model.HbciMapping;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPADel;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@Slf4j
public class DeleteStandingOrderJob extends ScaRequiredJob {

    @Override
    protected AbstractSEPAGV createSepaJob(SepaTransaction payment, PinTanPassport passport, String sepaPain) {
        StandingOrder standingOrder = (StandingOrder) payment;

        Konto src = passport.findAccountByAccountNumber(standingOrder.getSenderAccountNumber());
        src.iban = standingOrder.getSenderIban();
        src.bic = standingOrder.getSenderBic();

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        GVDauerSEPADel gvDauerSEPADel = new GVDauerSEPADel(passport, sepaPain);

        gvDauerSEPADel.setParam("src", src);
        gvDauerSEPADel.setParam("dst", dst);
        gvDauerSEPADel.setParam("btg", new Value(standingOrder.getAmount()));
        gvDauerSEPADel.setParam("usage", standingOrder.getUsage());

        gvDauerSEPADel.setParam("orderid", standingOrder.getOrderId());

        // standing order specific parameter
        if (standingOrder.getFirstExecutionDate() != null) {
            gvDauerSEPADel.setParam("firstdate", standingOrder.getFirstExecutionDate().toString());
        }
        if (standingOrder.getCycle() != null) {
            gvDauerSEPADel.setParam("timeunit", HbciMapping.cycleToTimeunit(standingOrder.getCycle())); // M month, W week
            gvDauerSEPADel.setParam("turnus", HbciMapping.cycleToTurnus(standingOrder.getCycle())); // 1W = every week, 2M = every two months
        }
        gvDauerSEPADel.setParam("execday", standingOrder.getExecutionDay()); // W: 1-7, M: 1-31
        if (standingOrder.getLastExecutionDate() != null) {
            gvDauerSEPADel.setParam("lastdate", standingOrder.getLastExecutionDate().toString());
        }

        gvDauerSEPADel.verifyConstraints();

        return gvDauerSEPADel;
    }

    @Override
    protected String getHbciJobName(SepaTransaction.TransactionType paymentType) {
        return GVDauerSEPADel.getLowlevelName();
    }

    @Override
    protected String orderIdFromJobResult(HBCIJobResult paymentGV) {
        return null;
    }
}
