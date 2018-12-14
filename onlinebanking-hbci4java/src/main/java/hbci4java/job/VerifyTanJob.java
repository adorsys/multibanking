package hbci4java.job;

import domain.AbstractPayment;
import domain.HBCIProduct;
import domain.TanChallenge;
import domain.request.VerifyTanRequest;
import domain.request.SendTanRequest;
import exception.HbciException;
import hbci4java.model.HbciCallback;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciPassport;
import hbci4java.model.HbciTanSubmit;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.Optional;

import static hbci4java.model.HbciDialogFactory.createDialog;
import static hbci4java.model.HbciDialogFactory.createPassport;

@Slf4j
public class VerifyTanJob extends AbstractTanProcessJob {

    public HbciTanSubmit sendTan(SendTanRequest request) {
        log.info("init hbci verify tan process");

        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();

        HbciCallback challengeCallback = new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge, String challenge_hhd_uc) {
                //needed later for submit
                hbciTanSubmit.setOrderRef(orderRef);
                if (challenge != null) {
                    hbciTanSubmit.setTanChallenge(TanChallenge.builder()
                            .title(challenge)
                            .data(challenge_hhd_uc)
                            .build());
                }
            }
        };

        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(request.getBankCode() != null ? request.getBankCode() : request.getBankAccess().getBankCode())
                .customerId(request.getBankAccess().getBankLogin())
                .login(request.getBankAccess().getBankLogin2())
                .hbciPassportState(request.getBankAccess().getHbciPassportState())
                .pin(request.getPin())
                .callback(challengeCallback)
                .build();

        dialogRequest.setHbciProduct(Optional.ofNullable(request.getHbciProduct())
                .map(product -> new HBCIProduct(product.getProduct(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(request.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        HBCITwoStepMechanism hbciTwoStepMechanism =
                dialog.getPassport().getBankTwostepMechanisms().get(request.getTanTransportType().getId());
        if (hbciTwoStepMechanism == null)
            throw new HbciException("inavalid two stem mechanism: " + request.getTanTransportType().getId());

        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        hktanProcess2(dialog, null, getOrderAccount(request, dialog.getPassport()), hktan);

        HBCIExecStatus status = dialog.execute(false);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());

        return hbciTanSubmit;
    }

    public void submit(VerifyTanRequest submitVerifyTanRequest) {
        HbciTanSubmit hbciTanSubmit = (HbciTanSubmit) submitVerifyTanRequest.getTanSubmit();

        HbciPassport.State state = HbciPassport.State.readJson(hbciTanSubmit.getPassportState());
        HbciPassport hbciPassport = createPassport(state.hbciVersion, state.blz, state.customerId, state.userId,
                state.hbciProduct,
                new HbciCallback() {

                    @Override
                    public String needTAN() {
                        return submitVerifyTanRequest.getTan();
                    }
                });
        state.apply(hbciPassport);
        hbciPassport.setPIN(submitVerifyTanRequest.getPin());

        HBCITwoStepMechanism hbciTwoStepMechanism =
                hbciPassport.getBankTwostepMechanisms().get(submitVerifyTanRequest.getTanTransportType().getId());
        hbciPassport.setCurrentSecMechInfo(hbciTwoStepMechanism);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());
        submitProcess2(hbciTanSubmit, hbciDialog);

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }
    }

    private Konto getOrderAccount(SendTanRequest scaRequest, PinTanPassport passport) {
        Konto orderAccount = passport.findAccountByAccountNumber(scaRequest.getBankAccount().getAccountNumber());
        orderAccount.iban = scaRequest.getBankAccount().getIban();
        orderAccount.bic = scaRequest.getBankAccount().getBic();
        return orderAccount;
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null; //TODO extract from
    }

    @Override
    AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain) {
        return null;
    }
}
