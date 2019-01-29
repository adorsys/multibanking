package hbci4java.job;

import domain.SepaTransaction;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;

public class EmptyJob extends ScaRequiredJob {

    @Override
    String getHbciJobName(SepaTransaction.TransactionType paymentType) {
        return null;
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    AbstractSEPAGV createSepaJob(SepaTransaction payment, PinTanPassport passport, String sepaPain) {
        return null;
    }
}
