package hbci4java.job;

import domain.AbstractScaTransaction;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;

public class EmptyJob extends ScaRequiredJob {

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return null;
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport, String rawData) {
        return null;
    }
}
