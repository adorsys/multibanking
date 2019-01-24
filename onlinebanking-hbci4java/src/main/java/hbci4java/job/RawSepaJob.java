package hbci4java.job;

import domain.AbstractPayment;
import domain.RawSepaPayment;
import domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVRawSEPA;
import org.kapott.hbci.GV.GVTermUebSEPA;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class RawSepaJob extends AbstractPaymentJob {

    @Override
    String getJobName(AbstractPayment.PaymentType paymentType) {
        return GVRawSEPA.getLowlevelName();
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain) {
        RawSepaPayment singlePayment = (RawSepaPayment) payment;

        GVRawSEPA sepagv = new GVRawSEPA(passport, GVUebSEPA.getLowlevelName(), singlePayment.getSepaPain());

        sepagv.verifyConstraints();

        return sepagv;
    }
}
