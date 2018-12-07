/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbci4java.job;

import domain.AbstractPayment;
import domain.HBCIProduct;
import domain.PaymentChallenge;
import domain.PaymentRequest;
import domain.request.SubmitPaymentRequest;
import exception.HbciException;
import hbci4java.model.HbciCallback;
import hbci4java.model.HbciDialogRequest;
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

import static hbci4java.model.HbciDialogFactory.createDialog;
import static hbci4java.model.HbciDialogFactory.createPassport;
import static org.kapott.hbci.manager.HBCIJobFactory.newJob;

public abstract class AbstractPaymentJob {

    abstract AbstractSEPAGV createPaymentJob(AbstractPayment payment, PinTanPassport passport, String sepaPain);

    abstract String getJobName(AbstractPayment.PaymentType paymentType);

    abstract String orderIdFromJobResult(HBCIJobResult paymentGV);

    public HbciTanSubmit execute(PaymentRequest paymentRequest) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        hbciTanSubmit.setOriginJobName(getJobName(paymentRequest.getPayment().getPaymentType()));

        HbciCallback hbciCallback = new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge, String challenge_hhd_uc) {
                //needed later for submit
                hbciTanSubmit.setOrderRef(orderRef);
                if (challenge != null) {
                    hbciTanSubmit.setPaymentChallenge(PaymentChallenge.builder()
                            .title(challenge)
                            .data(challenge_hhd_uc)
                            .build());
                }
            }
        };

        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(paymentRequest.getBankCode() != null ? paymentRequest.getBankCode() :
                        paymentRequest.getBankAccess().getBankCode())
                .customerId(paymentRequest.getBankAccess().getBankLogin())
                .login(paymentRequest.getBankAccess().getBankLogin2())
                .hbciPassportState(paymentRequest.getBankAccess().getHbciPassportState())
                .pin(paymentRequest.getPin())
                .callback(hbciCallback)
                .build();
        dialogRequest.setHbciProduct(Optional.ofNullable(paymentRequest.getHbciProduct())
                .map(product -> new HBCIProduct(product.getProduct(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(paymentRequest.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        HBCITwoStepMechanism hbciTwoStepMechanism =
                dialog.getPassport().getBankTwostepMechanisms().get(paymentRequest.getPayment().getTanMedia().getId());
        if (hbciTwoStepMechanism == null)
            throw new HbciException("inavalid two stem mechanism: " + paymentRequest.getPayment().getTanMedia().getId());

        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        AbstractSEPAGV uebSEPA = createPaymentJob(paymentRequest.getPayment(), dialog.getPassport(), null);

        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (hbciTwoStepMechanism.getProcess() == 1) {
            hktanProcess1(hbciTanSubmit, hbciTwoStepMechanism, uebSEPA, hktan);
            dialog.addTask(hktan, false);
        } else {
            hktanProcess2(dialog, uebSEPA, paymentRequest.getPayment(), hktan);
        }

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", paymentRequest.getPayment().getTanMedia().getMedium());
        }

        HBCIExecStatus status = dialog.execute(false);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        hbciTanSubmit.setPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        hbciTanSubmit.setDialogId(dialog.getDialogID());
        hbciTanSubmit.setMsgNum(dialog.getMsgnum());
        hbciTanSubmit.setOriginSegVersion(uebSEPA.getSegVersion());

        return hbciTanSubmit;
    }

    private void hktanProcess1(HbciTanSubmit hbciTanSubmit, HBCITwoStepMechanism
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

    private void hktanProcess2(HBCIDialog dialog, AbstractSEPAGV sepagv, AbstractPayment payment, GVTAN2Step hktan) {
        Konto src = dialog.getPassport().findAccountByAccountNumber(payment.getSenderAccountNumber());
        src.iban = payment.getSenderIban();
        src.bic = payment.getSenderBic();

        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        hktan.setParam("process", "4");
        hktan.setParam("orderaccount", src);

        List<AbstractHBCIJob> messages = dialog.addTask(sepagv);
        messages.add(hktan);
    }

    public String execute(SubmitPaymentRequest submitPaymentRequest) {
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
                hbciPassport.getBankTwostepMechanisms().get(submitPaymentRequest.getPayment().getTanMedia().getId());
        hbciPassport.setCurrentSecMechInfo(hbciTwoStepMechanism);

        HBCIDialog hbciDialog = new HBCIDialog(hbciPassport, hbciTanSubmit.getDialogId(), hbciTanSubmit.getMsgNum());
        AbstractHBCIJob paymentGV;

        if (hbciTwoStepMechanism.getProcess() == 1) {
            paymentGV = paymentProcess1(submitPaymentRequest.getPayment(), hbciTanSubmit, hbciPassport, hbciDialog);
        } else {
            paymentGV = paymentProcess2(hbciTanSubmit, hbciDialog);
        }

        HBCIExecStatus status = hbciDialog.execute(true);
        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        } else {
            return orderIdFromJobResult(paymentGV.getJobResult());
        }
    }

    private AbstractHBCIJob paymentProcess1(AbstractPayment payment, HbciTanSubmit hbciTanSubmit, HbciPassport
            hbciPassport, HBCIDialog hbciDialog) {
        //1. Schritt: HKTAN <-> HITAN
        //2. Schritt: HKUEB <-> HIRMS zu HKUEB
        AbstractHBCIJob uebSEPAJob = createPaymentJob(payment, hbciPassport, hbciTanSubmit.getSepaPain());
        hbciDialog.addTask(uebSEPAJob);
        return uebSEPAJob;
    }

    private AbstractHBCIJob paymentProcess2(HbciTanSubmit hbciTanSubmit, HBCIDialog hbciDialog) {
        //Schritt 1: HKUEB und HKTAN <-> HITAN
        //Schritt 2: HKTAN <-> HITAN und HIRMS zu HIUEB
        AbstractHBCIJob originJob = newJob(hbciTanSubmit.getOriginJobName(), hbciDialog.getPassport());
        originJob.setSegVersion(hbciTanSubmit.getOriginSegVersion());

        GVTAN2Step hktan = new GVTAN2Step(hbciDialog.getPassport());
        hktan.setOriginJob(originJob);
        hktan.setParam("orderref", hbciTanSubmit.getOrderRef());
        hktan.setParam("process", "2");
        hktan.setParam("notlasttan", "N");
        hbciDialog.addTask(hktan, false);
        return originJob;
    }
}
