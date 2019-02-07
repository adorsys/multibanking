package hbci4java.job;

import domain.AbstractScaTransaction;
import domain.RawSepaPayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVRawSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

public class RawSepaJob extends ScaRequiredJob {

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType paymentType) {
        return GVRawSEPA.getLowlevelName();
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    AbstractSEPAGV createHbciJob(AbstractScaTransaction transaction, PinTanPassport passport, String rawData) {
        RawSepaPayment singlePayment = (RawSepaPayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        GVRawSEPA sepagv = new GVRawSEPA(passport, GVUebSEPA.getLowlevelName(), singlePayment.getRawData());
        sepagv.setParam("src", src);

        sepagv.verifyConstraints();

        return sepagv;
    }
}
