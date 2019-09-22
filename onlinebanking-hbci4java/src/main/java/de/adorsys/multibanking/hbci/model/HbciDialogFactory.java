/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.dialog.AbstractHbciDialog;
import org.kapott.hbci.dialog.HBCIBpdDialog;
import org.kapott.hbci.dialog.HBCIJobsDialog;
import org.kapott.hbci.dialog.HBCIUpdDialog;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIProduct;
import org.kapott.hbci.manager.HBCITwoStepMechanism;
import org.kapott.hbci.manager.HBCIUtils;

import java.util.HashMap;
import java.util.Optional;

import static de.adorsys.multibanking.domain.exception.MultibankingError.BANK_NOT_SUPPORTED;

@UtilityClass
public class HbciDialogFactory {

    public static AbstractHbciDialog createDialog(HbciDialogType dialogType, HbciPassport existingPassport,
                                                  HbciDialogRequest dialogRequest,
                                                  HBCITwoStepMechanism twoStepMechanism) {
        String bankCode = dialogRequest.getBank().getBankApiBankCode() != null
            ? dialogRequest.getBank().getBankApiBankCode()
            : dialogRequest.getBank().getBankCode();

        BankInfo bankInfo = Optional.ofNullable(HBCIUtils.getBankInfo(dialogRequest.getBank().getBankCode()))
            .orElseThrow(() -> new MultibankingException(BANK_NOT_SUPPORTED,
                "Bank [" + bankCode + "] not supported"));

        HBCIProduct hbciProduct = Optional.ofNullable(dialogRequest.getHbciProduct())
            .map(product -> new HBCIProduct(product.getName(), product.getVersion()))
            .orElse(null);

        HbciPassport newPassport = Optional.ofNullable(existingPassport)
            .orElseGet(() -> {
                HbciConsent hbciConsent = (HbciConsent) dialogRequest.getBankApiConsentData();

                return createPassport(bankInfo.getPinTanVersion().getId(), bankCode,
                    hbciConsent.getCredentials().getUserId(), hbciConsent.getCredentials().getCustomerId(),
                    hbciProduct, dialogRequest.getCallback());
            });
        newPassport.setCurrentSecMechInfo(twoStepMechanism);

        Optional.ofNullable(dialogRequest.getBankAccess())
            .map(BankAccess::getHbciPassportState)
            .ifPresent(state -> HbciPassport.State.fromJson(state).apply(newPassport));

        Optional.ofNullable(dialogRequest.getHbciBPD())
            .ifPresent(newPassport::setBPD);

        Optional.ofNullable(dialogRequest.getHbciUPD())
            .ifPresent(newPassport::setUPD);

        Optional.ofNullable(dialogRequest.getHbciSysId())
            .ifPresent(newPassport::setSysId);

        newPassport.setPIN(((HbciConsent) dialogRequest.getBankApiConsentData()).getCredentials().getPin());

        String url = bankInfo.getPinTanAddress();
        String proxyPrefix = System.getProperty("proxyPrefix", null);
        if (proxyPrefix != null) {
            url = proxyPrefix + url;
        }
        newPassport.setHost(url);

        switch (dialogType) {
            case BPD:
                return new HBCIBpdDialog(newPassport);
            case UPD:
                return new HBCIUpdDialog(newPassport);
            case JOBS:
                return new HBCIJobsDialog(newPassport);
            default:
                throw new IllegalStateException("Unexpected dialog tpye: " + dialogType);
        }

    }

    public static HbciPassport createPassport(HbciPassport.State state, HBCICallback callback) {
        return createPassport(state.getHbciVersion(), state.getBlz(), state.getCustomerId(), state.getUserId(),
            state.getHbciProduct(), callback);
    }

    private static HbciPassport createPassport(String hbciVersion, String bankCode, String customerId, String userId,
                                               HBCIProduct hbciProduct, HBCICallback callback) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef," +
            "HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");
        properties.put("client.errors.ignoreJobNotSupported", "yes");

        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", bankCode);
        properties.put("client.passport.customerId", customerId);
        properties.put("client.errors.ignoreCryptErrors", "yes");

        if (StringUtils.isNotBlank(userId)) {
            properties.put("client.passport.userId", userId);
        }

        return new HbciPassport(hbciVersion, properties, callback, hbciProduct);
    }

}
