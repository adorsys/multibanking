package hbci4java.job;

import domain.BankAccess;
import domain.PaymentChallenge;
import domain.StandingOrder;
import exception.HbciException;
import hbci4java.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVDauerSEPANew;
import org.kapott.hbci.GV.GVTAN2Step;
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

@Slf4j
public class HbciNewStandingOrderJob {

    public static HbciTanSubmit createStandingOrder(BankAccess bankAccess, String bankCode, String pin, StandingOrder standingOrder) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        hbciTanSubmit.setOriginJobName("DauerSEPANew");

        HbciCallback hbciCallback = new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge) {
                //needed later for submit
                hbciTanSubmit.setOrderRef(orderRef);
                if (challenge != null) {
                    standingOrder.setPaymentChallenge(PaymentChallenge.builder()
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

        HBCITwoStepMechanism hbciTwoStepMechanism = dialog.getPassport().getBankTwostepMechanisms().get(standingOrder.getTanMedia().getId());
        if (hbciTwoStepMechanism == null) {
            throw new HbciException("inavalid two stem mechanism: " + standingOrder.getTanMedia().getId());
        }
        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        Konto src = dialog.getPassport().findAccountByAccountNumber(standingOrder.getSenderAccountNumber());
        src.iban = standingOrder.getSenderIban();
        src.bic = standingOrder.getSenderBic();

        GVDauerSEPANew standingorderSEPA = createStandingOrderSEPAJob(standingOrder, dialog.getPassport(), src, null);

        GVTAN2Step hktan = (GVTAN2Step) newJob("TAN2Step", dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (hbciTwoStepMechanism.getProcess() == 1) {
            hktanProcess1(hbciTanSubmit, hbciTwoStepMechanism, standingorderSEPA, hktan);
            dialog.addTask(hktan, false);
        } else {
            hktanProcess2(dialog, src, standingorderSEPA, hktan);
        }

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", standingOrder.getTanMedia().getMedium());
        }

        HBCIExecStatus status = dialog.execute(false);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setOriginSegVersion(standingorderSEPA.getSegVersion());

        return hbciTanSubmit;
    }

    private static void hktanProcess2(HBCIDialog dialog, Konto src, AbstractSEPAGV standingorderSEPA, GVTAN2Step hktan) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("orderaccount", src);

        List<AbstractHBCIJob> messages = dialog.addTask(standingorderSEPA);
        messages.add(hktan);
    }

    private static void hktanProcess1(HbciTanSubmit hbciTanSubmit, HBCITwoStepMechanism hbciTwoStepMechanism, AbstractSEPAGV standingorderSEPA, GVTAN2Step hktan) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        hktan.setParam("process", hbciTwoStepMechanism.getProcess());
        hktan.setParam("notlasttan", "N");
        hktan.setParam("orderhash", standingorderSEPA.createOrderHash(hbciTwoStepMechanism.getSegversion()));

        hbciTanSubmit.setSepaPain(standingorderSEPA.getPainXml());

        // wenn needchallengeklass gesetzt ist:
        if (StringUtils.equals(hbciTwoStepMechanism.getNeedchallengeklass(), "J")) {
            ChallengeInfo cinfo = ChallengeInfo.getInstance();
            cinfo.applyParams(standingorderSEPA, hktan, hbciTwoStepMechanism);
        }
    }

    public static void submitStandingOrder(StandingOrder standingOrder, HbciTanSubmit hbciTanSubmit, String pin, String tan) {
        HbciPassport.State state = HbciPassport.State.readJson(hbciTanSubmit.getPassportState());
        HbciPassport hbciPassport = createPassport(state.hbciVersion, state.blz, state.customerId, state.userId, new HbciCallback() {

            @Override
            public String needTAN() {
                return tan;
            }
        });
        state.apply(hbciPassport);
        hbciPassport.setPIN(pin);

        HBCITwoStepMechanism hbciTwoStepMechanism = hbciPassport.getBankTwostepMechanisms().get(standingOrder.getTanMedia().getId());
        hbciPassport.setCurrentSecMechInfo(hbciTwoStepMechanism);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());


        if (hbciTwoStepMechanism.getProcess() == 1) {
            standingorderSepaProcess1(standingOrder, hbciTanSubmit, hbciPassport, hbciDialog);
        } else {
            standingorderSepaProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }
    }

    private static void standingorderSepaProcess2(HbciTanSubmit hbciTanSubmit, HBCIDialog hbciDialog) {
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

    private static void standingorderSepaProcess1(StandingOrder standingOrder, HbciTanSubmit hbciTanSubmit, HbciPassport hbciPassport, HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN  HITAN
        //2. Schritt: HKUEB  HIRMS zu HKUEB
        Konto src = hbciPassport.findAccountByAccountNumber(standingOrder.getSenderAccountNumber());
        src.iban = standingOrder.getSenderIban();
        src.bic = standingOrder.getSenderBic();

        AbstractHBCIJob standingorderSEPAJob = createStandingOrderSEPAJob(standingOrder, hbciPassport, src, hbciTanSubmit.getSepaPain());
        hbciDialog.addTask(standingorderSEPAJob);
    }

    private static GVDauerSEPANew createStandingOrderSEPAJob(StandingOrder standingOrder, PinTanPassport passport, Konto src, String sepaPain) {
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
}
