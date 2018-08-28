package hbci4java.job;

import domain.BankAccess;
import domain.Payment;
import domain.PaymentChallenge;
import exception.HbciException;
import hbci4java.HbciCallback;
import hbci4java.HbciDialogRequest;
import hbci4java.HbciPassport;
import hbci4java.HbciTanSubmit;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV.GVUebSEPA;
import org.kapott.hbci.manager.ChallengeInfo;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.util.List;

import static hbci4java.HbciDialogFactory.createDialog;
import static hbci4java.HbciDialogFactory.createPassport;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

public class HbciSinglePaymentJob {

    public static void createPayment(BankAccess bankAccess, String bankCode, String pin, Payment payment) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        hbciTanSubmit.setOriginJobName("UebSEPA");

        HbciCallback hbciCallback = new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge) {
                //needed later for submit
                hbciTanSubmit.setOrderRef(orderRef);
                if (challenge != null) {
                    payment.setPaymentChallenge(PaymentChallenge.builder()
                            .title(challenge)
                            .build());
                }
            }
        };

        HBCIDialog dialog = createDialog(HbciDialogRequest.builder()
                .bankCode(bankCode != null ? bankCode : bankAccess.getBankCode())
                .customerId(bankAccess.getBankLogin())
                .login(bankAccess.getBankLogin2())
                .hbciPassportState(bankAccess.getHbciPassportState())
                .pin(pin)
                .build(), hbciCallback);

        HBCITwoStepMechanism hbciTwoStepMechanism = dialog.getPassport().getBankTwostepMechanisms().get(payment.getTanMedia().getId());
        if (hbciTwoStepMechanism == null)
            throw new HbciException("inavalid two stem mechanism: " + payment.getTanMedia().getId());

        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        Konto src = dialog.getPassport().findAccountByAccountNumber(payment.getSenderAccountNumber());
        src.iban = payment.getSenderIban();
        src.bic = payment.getSenderBic();

        GVUebSEPA uebSEPA = createUebSEPAJob(payment, dialog.getPassport(), src, null);

        GVTAN2Step hktan = (GVTAN2Step) newJob("TAN2Step", dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (hbciTwoStepMechanism.getProcess() == 1) {
            hktanProcess1(hbciTanSubmit, hbciTwoStepMechanism, uebSEPA, hktan);
            dialog.addTask(hktan, false);
        } else {
            hktanProcess2(dialog, src, uebSEPA, hktan);
        }

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", payment.getTanMedia().getMedium());
        }

        HBCIExecStatus status = dialog.execute(false);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setOriginSegVersion(uebSEPA.getSegVersion());
        payment.setTanSubmitExternal(hbciTanSubmit);
    }

    private static void hktanProcess2(HBCIDialog dialog, Konto src, GVUebSEPA uebSEPA, GVTAN2Step hktan) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("orderaccount", src);

        List<AbstractHBCIJob> messages = dialog.addTask(uebSEPA);
        messages.add(hktan);
    }

    private static void hktanProcess1(HbciTanSubmit hbciTanSubmit, HBCITwoStepMechanism
            hbciTwoStepMechanism, GVUebSEPA uebSEPA, GVTAN2Step hktan) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("process", hbciTwoStepMechanism.getProcess());
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", uebSEPA.createOrderHash(hbciTwoStepMechanism.getSegversion()));

        hbciTanSubmit.setSepaPain(uebSEPA.getPainXml());

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(uebSEPA, hktan, hbciTwoStepMechanism);
        }
    }

    public static void submitPayment(Payment payment, String pin, String tan) {
        HbciTanSubmit hbciTanSubmit = (HbciTanSubmit) payment.getTanSubmitExternal();

        HbciPassport.State state = HbciPassport.State.readJson(hbciTanSubmit.getPassportState());
        HbciPassport hbciPassport = createPassport(state.hbciVersion, state.blz, state.customerId, state.userId, new HbciCallback() {

            @Override
            public String needTAN() {
                return tan;
            }
        });
        state.apply(hbciPassport);
        hbciPassport.setPIN(pin);

        HBCITwoStepMechanism hbciTwoStepMechanism = hbciPassport.getBankTwostepMechanisms().get(payment.getTanMedia().getId());
        hbciPassport.setCurrentSecMechInfo(hbciTwoStepMechanism);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());


        if (hbciTwoStepMechanism.getProcess() == 1) {
            uebSepaProcess1(payment, hbciTanSubmit, hbciPassport, hbciDialog);
        } else {
            uebSepaProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }
    }

    private static void uebSepaProcess2(HbciTanSubmit hbciTanSubmit, HBCIDialog hbciDialog) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        AbstractHBCIJob originJob = newJob(hbciTanSubmit.getOriginJobName(), hbciDialog.getPassport());
        originJob.setSegVersion(hbciTanSubmit.getOriginSegVersion());

        GVTAN2Step hktan = (GVTAN2Step) newJob("TAN2Step", hbciDialog.getPassport());
        hktan.setOriginJob(originJob);
        hktan.setParam("orderref", hbciTanSubmit.getOrderRef());
        hktan.setParam("process", "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
    }

    private static void uebSepaProcess1(Payment payment, HbciTanSubmit hbciTanSubmit, HbciPassport
            hbciPassport, HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN  HITAN
        //2. Schritt: HKUEB  HIRMS zu HKUEB
        Konto src = hbciPassport.findAccountByAccountNumber(payment.getSenderAccountNumber());
        src.iban = payment.getSenderIban();
        src.bic = payment.getSenderBic();

        AbstractHBCIJob uebSEPAJob = createUebSEPAJob(payment, hbciPassport, src, hbciTanSubmit.getSepaPain());
        hbciDialog.addTask(uebSEPAJob);
    }

    private static GVUebSEPA createUebSEPAJob(Payment payment, PinTanPassport passport, Konto src, String sepaPain) {
        Konto dst = new Konto();
        dst.name = payment.getReceiver();
        dst.iban = payment.getReceiverIban();
        dst.bic = payment.getReceiverBic();

        GVUebSEPA uebSEPA = new GVUebSEPA(passport, GVUebSEPA.getLowlevelName(), sepaPain);
        uebSEPA.setParam("src", src);
        uebSEPA.setParam("dst", dst);
        uebSEPA.setParam("btg", new Value(payment.getAmount()));
        uebSEPA.setParam("usage", payment.getPurpose());

        uebSEPA.verifyConstraints();

        return uebSEPA;
    }
}
