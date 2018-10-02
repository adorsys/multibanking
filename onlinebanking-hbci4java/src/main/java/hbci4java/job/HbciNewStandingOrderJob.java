package hbci4java.job;

import domain.AbstractPayment;
import domain.StandingOrder;
import hbci4java.HbciMapping;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

@Slf4j
public class HbciNewStandingOrderJob extends AbstractPaymentJob {

    @Override
    protected AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain) {
        StandingOrder standingOrder = (StandingOrder) payment;

        Konto src = passport.findAccountByAccountNumber(standingOrder.getSenderAccountNumber());
        src.iban = standingOrder.getSenderIban();
        src.bic = standingOrder.getSenderBic();

        Konto dst = new Konto();
        dst.name = standingOrder.getOtherAccount().getOwner();
        dst.iban = standingOrder.getOtherAccount().getIban();
        dst.bic = standingOrder.getOtherAccount().getBic();

        GVDauerSEPANew gvDauerSEPANew = new GVDauerSEPANew(passport, sepaPain);

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
    protected String getJobName() {
        return GVDauerSEPANew.getLowlevelName();
    }
}
