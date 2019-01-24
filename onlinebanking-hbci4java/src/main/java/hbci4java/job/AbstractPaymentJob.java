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
import domain.PaymentRequest;
import domain.TanChallenge;
import exception.HbciException;
import hbci4java.model.HbciCallback;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciPassport;
import hbci4java.model.HbciTanSubmit;
import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.manager.HHDVersion;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.Optional;

import static hbci4java.model.HbciDialogFactory.createDialog;

public abstract class AbstractPaymentJob extends AbstractTanProcessJob {

    abstract String getJobName(AbstractPayment.PaymentType paymentType);

    public HbciTanSubmit init(PaymentRequest paymentRequest) {
        HbciTanSubmit hbciTanSubmit = new HbciTanSubmit();
        hbciTanSubmit.setOriginJobName(getJobName(paymentRequest.getPayment().getPaymentType()));

        HbciCallback hbciCallback = new HbciCallback() {

            @Override
            public void tanChallengeCallback(String orderRef, String challenge, String challenge_hhd_uc,
                                             HHDVersion.Type type) {
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
                dialog.getPassport().getBankTwostepMechanisms().get(paymentRequest.getTanTransportType().getId());
        if (hbciTwoStepMechanism == null)
            throw new HbciException("inavalid two stem mechanism: " + paymentRequest.getTanTransportType().getId());

        dialog.getPassport().setCurrentSecMechInfo(hbciTwoStepMechanism);

        AbstractSEPAGV uebSEPA = createPaymentJob(paymentRequest.getPayment(), dialog.getPassport(), null);

        GVTAN2Step hktan = new GVTAN2Step(dialog.getPassport());
        hktan.setSegVersion(hbciTwoStepMechanism.getSegversion());

        if (hbciTwoStepMechanism.getProcess() == 1) {
            hbciTanSubmit.setSepaPain(hktanProcess1(hbciTwoStepMechanism, uebSEPA, hktan));
            dialog.addTask(hktan, false);
        } else {
            hktanProcess2(dialog, uebSEPA, getOrderAccount(paymentRequest, dialog.getPassport()), hktan);
        }

        if (dialog.getPassport().tanMediaNeeded()) {
            hktan.setParam("tanmedia", paymentRequest.getTanTransportType().getMedium());
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

    private Konto getOrderAccount(PaymentRequest paymentRequest, PinTanPassport passport) {
        Konto orderAccount = passport.findAccountByAccountNumber(paymentRequest.getPayment().getSenderAccountNumber());
        orderAccount.iban = paymentRequest.getPayment().getSenderIban();
        orderAccount.bic = paymentRequest.getPayment().getSenderBic();
        return orderAccount;
    }

}
