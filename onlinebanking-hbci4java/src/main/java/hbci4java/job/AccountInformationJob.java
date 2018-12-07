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

import domain.BankAccount;
import domain.BankApi;
import domain.HBCIProduct;
import domain.TanTransportType;
import domain.request.LoadAccountInformationRequest;
import domain.response.LoadAccountInformationResponse;
import exception.HbciException;
import hbci4java.model.HbciCallback;
import hbci4java.model.HbciDialogRequest;
import hbci4java.model.HbciMapping;
import hbci4java.model.HbciPassport;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.GVSEPAInfo;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.*;
import java.util.stream.Collectors;

import static hbci4java.model.HbciDialogFactory.createDialog;

@Slf4j
public class AccountInformationJob {

    public static LoadAccountInformationResponse loadBankAccounts(LoadAccountInformationRequest request,
                                                                  HbciCallback callback) {
        log.info("Loading account list for bank [{}]", request.getBankCode());

        HbciDialogRequest dialogRequest = HbciDialogRequest.builder()
                .bankCode(request.getBankCode() != null ? request.getBankCode() : request.getBankAccess().getBankCode())
                .customerId(request.getBankAccess().getBankLogin())
                .login(request.getBankAccess().getBankLogin2())
                .hbciPassportState(request.getBankAccess().getHbciPassportState())
                .pin(request.getPin())
                .callback(callback)
                .build();

        dialogRequest.setHbciProduct(Optional.ofNullable(request.getHbciProduct())
                .map(product -> new HBCIProduct(product.getProduct(), product.getVersion()))
                .orElse(null));
        dialogRequest.setBpd(request.getBpd());

        HBCIDialog dialog = createDialog(null, dialogRequest);

        if (!dialog.getPassport().jobSupported("SEPAInfo"))
            throw new RuntimeException("SEPAInfo job not supported");

        log.info("fetching SEPA informations");
        dialog.addTask(new GVSEPAInfo(dialog.getPassport()));

        // TAN-Medien abrufen
        if (request.isUpdateTanTransportTypes()) {
            if (dialog.getPassport().jobSupported("TANMediaList")) {
                log.info("fetching TAN media list");
                dialog.addTask(new GVTANMediaList(dialog.getPassport()));
            }
        }

        HBCIExecStatus status = dialog.execute(true);

        if (!status.isOK()) {
            throw new HbciException(status.getDialogStatus().getErrorString());
        }

        request.getBankAccess().setBankName(dialog.getPassport().getInstName());
        List<BankAccount> hbciAccounts = new ArrayList<>();
        for (Konto konto : dialog.getPassport().getAccounts()) {
            BankAccount bankAccount = HbciMapping.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(request.getBankAccess().getBankName());
            hbciAccounts.add(bankAccount);
        }

        if (request.isUpdateTanTransportTypes()) {
            extractTanTransportTypes(dialog.getPassport()).ifPresent(tanTransportTypes -> {
                if (request.getBankAccess().getTanTransportTypes() == null) {
                    request.getBankAccess().setTanTransportTypes(new HashMap<>());
                }
                request.getBankAccess().getTanTransportTypes().put(BankApi.HBCI, tanTransportTypes);
            });
        }

        request.getBankAccess().setHbciPassportState(new HbciPassport.State(dialog.getPassport()).toJson());
        return LoadAccountInformationResponse.builder()
                .bankAccess(request.getBankAccess())
                .bankAccounts(hbciAccounts)
                .build();
    }

    public static Optional<List<TanTransportType>> extractTanTransportTypes(PinTanPassport hbciPassport) {
        return Optional.ofNullable(hbciPassport.getUPD())
                .map(upd -> hbciPassport.getUserTwostepMechanisms()
                        .stream()
                        .map(id -> hbciPassport.getBankTwostepMechanisms().get(id))
                        .filter(Objects::nonNull)
                        .map(hbciTwoStepMechanism -> TanTransportType.builder()
                                .id(hbciTwoStepMechanism.getSecfunc())
                                .name(hbciTwoStepMechanism.getName())
                                .inputInfo(hbciTwoStepMechanism.getInputinfo())
                                .medium(hbciPassport.getTanMedia(hbciTwoStepMechanism.getId()) != null ?
                                        hbciPassport.getTanMedia(hbciTwoStepMechanism.getId()).mediaName : null)
                                .build())
                        .collect(Collectors.toList()));

    }
}
