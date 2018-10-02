package hbci4java.job;

import domain.AbstractPayment;
import domain.SinglePayment;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

public class HbciSinglePaymentJob extends AbstractPaymentJob {

    @Override
    protected AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain) {
        SinglePayment singlePayment = (SinglePayment) payment;

        Konto src = passport.findAccountByAccountNumber(payment.getSenderAccountNumber());
        src.iban = payment.getSenderIban();
        src.bic = payment.getSenderBic();

        Konto dst = new Konto();
        dst.name = singlePayment.getReceiver();
        dst.iban = singlePayment.getReceiverIban();
        dst.bic = singlePayment.getReceiverBic();

        GVUebSEPA uebSEPA = new GVUebSEPA(passport, GVUebSEPA.getLowlevelName(), sepaPain);
        uebSEPA.setParam("src", src);
        uebSEPA.setParam("dst", dst);
        uebSEPA.setParam("btg", new Value(singlePayment.getAmount()));
        uebSEPA.setParam("usage", singlePayment.getPurpose());

        uebSEPA.verifyConstraints();

        return uebSEPA;
    }

    @Override
    protected String getJobName() {
        return GVUebSEPA.getLowlevelName();
    }
}
