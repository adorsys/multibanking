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

package de.adorsys.multibanking.hbci.job;

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApi;
import de.adorsys.multibanking.domain.TanTransportType;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.LoadAccountInformationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.hbci.model.HbciMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSEPAInfo;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class AccountInformationJob extends ScaRequiredJob {

    private final LoadAccountInformationRequest loadAccountInformationRequest;

    private List<BankAccount> hbciAccounts;

    public static List<TanTransportType> extractTanTransportTypes(PinTanPassport hbciPassport) {
        return hbciPassport.getUserTwostepMechanisms()
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
            .collect(Collectors.toList());
    }

    @Override
    AbstractHBCIJob createHbciJob(PinTanPassport passport) {
        if (!passport.jobSupported("SEPAInfo"))
            throw new MultibankingException(HBCI_ERROR, "SEPAInfo job not supported");

        return new GVSEPAInfo(passport);
    }

    @Override
    void beforeExecute(HBCIDialog dialog) {
        // TAN-Medien abrufen
        if (loadAccountInformationRequest.isUpdateTanTransportTypes() && dialog.getPassport().jobSupported(
            "TANMediaList")) {
            log.info("fetching TAN media list");
            dialog.addTask(new GVTANMediaList(dialog.getPassport()));
        }
    }

    @Override
    void afterExecute(HBCIDialog dialog) {
        loadAccountInformationRequest.getBankAccess().setBankName(dialog.getPassport().getInstName());

        hbciAccounts = new ArrayList<>();
        for (Konto konto : dialog.getPassport().getAccounts()) {
            BankAccount bankAccount = HbciMapping.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(loadAccountInformationRequest.getBankAccess().getBankName());
            hbciAccounts.add(bankAccount);
        }

        if (loadAccountInformationRequest.isUpdateTanTransportTypes()) {
            loadAccountInformationRequest.getBankAccess().setTanTransportTypes(new HashMap<>());
            loadAccountInformationRequest.getBankAccess().getTanTransportTypes().put(BankApi.HBCI,
                extractTanTransportTypes(dialog.getPassport()));
        }
    }

    @Override
    TransactionRequest getTransactionRequest() {
        return loadAccountInformationRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return GVSEPAInfo.getLowlevelName();
    }

    @Override
    String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    public LoadAccountInformationResponse createResponse() {
        return LoadAccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(hbciAccounts)
            .build();
    }
}
