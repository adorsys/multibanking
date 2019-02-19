package hbci4java.job;

import domain.AbstractScaTransaction;
import domain.RawSepaPayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPANew;
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
        RawSepaPayment sepaPayment = (RawSepaPayment) transaction;

        Konto src = getDebtorAccount(transaction, passport);

        String jobName;
        switch (sepaPayment.getSepaTransactionType()) {
            case SINGLE_PAYMENT:
                jobName = GVUebSEPA.getLowlevelName();
                break;
            case BULK_PAYMENT:
                jobName = "SammelUebSEPA";
                break;
            case STANDING_ORDER:
                jobName = GVDauerSEPANew.getLowlevelName();
                break;
            default:
                throw new IllegalArgumentException("unsupported raw sepa transaction: " + transaction.getTransactionType());
        }

        GVRawSEPA sepagv = new GVRawSEPA(passport, jobName, sepaPayment.getRawData());
        sepagv.setParam("src", src);

        sepagv.verifyConstraints();

        return sepagv;
    }

    public enum PaymentType {
        SINGLE, FUTURE, BULK, PERIODIC
    }
}
