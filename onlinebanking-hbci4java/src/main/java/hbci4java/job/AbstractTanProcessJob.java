package hbci4java.job;

import domain.AbstractPayment;
import domain.request.SubmitPaymentRequest;
import exception.HbciException;
import hbci4java.model.HbciCallback;
import hbci4java.model.HbciPassport;
import hbci4java.model.HbciTanSubmit;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.ChallengeInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.List;
import java.util.Optional;

import static hbci4java.model.HbciDialogFactory.createPassport;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

public abstract class AbstractTanProcessJob {

    public void hktanProcess1(HbciTanSubmit hbciTanSubmit, HBCITwoStepMechanism
            hbciTwoStepMechanism, AbstractSEPAGV sepagv, GVTAN2Step hktan) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("process", hbciTwoStepMechanism.getProcess());
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", sepagv.createOrderHash(hbciTwoStepMechanism.getSegversion()));

        hbciTanSubmit.setSepaPain(sepagv.getPainXml());

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(sepagv, hktan, hbciTwoStepMechanism);
        }
    }

    public void hktanProcess2(HBCIDialog dialog, AbstractSEPAGV sepagv, Konto orderAccount, GVTAN2Step hktan) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("orderaccount", orderAccount);

        Optional<List<AbstractHBCIJob>> messages = Optional.ofNullable(sepagv)
                .map(abstractSEPAGV -> dialog.addTask(abstractSEPAGV));

        if (messages.isPresent()) {
            messages.get().add(hktan);
        } else {
            dialog.addTask(hktan);
        }
    }

    public String submit(SubmitPaymentRequest submitPaymentRequest) {
        HbciTanSubmit hbciTanSubmit = (HbciTanSubmit) submitPaymentRequest.getTanSubmit();

        HbciPassport.State state = HbciPassport.State.readJson(hbciTanSubmit.getPassportState());
        HbciPassport hbciPassport = createPassport(state.hbciVersion, state.blz, state.customerId, state.userId,
                state.hbciProduct,
                new HbciCallback() {

                    @Override
                    public String needTAN() {
                        return submitPaymentRequest.getTan();
                    }
                });
        state.apply(hbciPassport);
        hbciPassport.setPIN(submitPaymentRequest.getPin());

        HBCITwoStepMechanism hbciTwoStepMechanism =
                hbciPassport.getBankTwostepMechanisms().get(submitPaymentRequest.getTanTransportType().getId());
        hbciPassport.setCurrentSecMechInfo(hbciTwoStepMechanism);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());
        AbstractHBCIJob paymentGV;

        if (hbciTwoStepMechanism.getProcess() == 1) {
            paymentGV = submitProcess1(submitPaymentRequest.getPayment(), hbciTanSubmit, hbciPassport, hbciDialog);
        } else {
            paymentGV = submitProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        } else {
            return orderIdFromJobResult(paymentGV.getJobResult());
        }
    }

    private AbstractHBCIJob submitProcess1(AbstractPayment payment, HbciTanSubmit hbciTanSubmit, HbciPassport
            hbciPassport, HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        AbstractHBCIJob uebSEPAJob = createPaymentJob(payment, hbciPassport, hbciTanSubmit.getSepaPain());
        hbciDialog.addTask(uebSEPAJob);
        return uebSEPAJob;
    }

    public AbstractHBCIJob submitProcess2(HbciTanSubmit hbciTanSubmit, HBCIDialog hbciDialog) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        AbstractHBCIJob originJob = Optional.ofNullable(hbciTanSubmit.getOriginJobName())
                .map(s -> {
                    AbstractHBCIJob result = newJob(hbciTanSubmit.getOriginJobName(), hbciDialog.getPassport());
                    result.setSegVersion(hbciTanSubmit.getOriginSegVersion());
                    return result;
                }).orElse(null);

        GVTAN2Step hktan = new GVTAN2Step(hbciDialog.getPassport());
        hktan.setOriginJob(originJob);
        hktan.setParam("orderref", hbciTanSubmit.getOrderRef());
        hktan.setParam("process", "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
        return originJob;
    }

    abstract String orderIdFromJobResult(HBCIJobResult jobResult);

    abstract AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain);
}
