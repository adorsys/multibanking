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
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.LoadAccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.AbstractScaTransaction;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSEPAInfo;
import org.kapott.hbci.GV.GVTANMediaList;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class AccountInformationJob extends ScaRequiredJob<LoadAccounts, LoadAccountInformationResponse> {

    private final TransactionRequest<LoadAccounts> loadAccountInformationRequest;

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
                .needTanMedia(hbciTwoStepMechanism.getNeedtanmedia().equals("2"))
                .build())
            .map(tanTransportType -> {
                if (!tanTransportType.isNeedTanMedia()) {
                    return Collections.singletonList(tanTransportType);
                } else {
                    return hbciPassport.getTanMedias().stream()
                        .map(tanMediaInfo -> {
                            TanTransportType clone = SerializationUtils.clone(tanTransportType);
                            clone.setMedium(tanMediaInfo.mediaName);
                            return clone;
                        })
                        .collect(Collectors.toList());
                }
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    }

    @Override
    public AbstractHBCIJob createScaMessage(PinTanPassport passport) {
        if (!passport.jobSupported("SEPAInfo"))
            throw new MultibankingException(HBCI_ERROR, "SEPAInfo job not supported");

        return new GVSEPAInfo(passport);
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        // TAN-Medien abrufen
        if (loadAccountInformationRequest.getTransaction().isUpdateTanTransportTypes() && passport.jobSupported(GVTANMediaList.getLowlevelName())) {
            log.info("fetching TAN media list");
            return Collections.singletonList(new GVTANMediaList(passport));
        }
        return Collections.emptyList();
    }

    @Override
    TransactionRequest<LoadAccounts> getTransactionRequest() {
        return loadAccountInformationRequest;
    }

    @Override
    String getHbciJobName(AbstractScaTransaction.TransactionType transactionType) {
        return GVSEPAInfo.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public LoadAccountInformationResponse createJobResponse(PinTanPassport passport) {
        loadAccountInformationRequest.getBankAccess().setBankName(passport.getInstName());

        hbciAccounts = new ArrayList<>();
        for (Konto konto : passport.getAccounts()) {
            BankAccount bankAccount = hbciObjectMapper.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(loadAccountInformationRequest.getBankAccess().getBankName());
            hbciAccounts.add(bankAccount);
        }

        if (loadAccountInformationRequest.getTransaction().isUpdateTanTransportTypes()) {
            loadAccountInformationRequest.getBankAccess().setTanTransportTypes(new HashMap<>());
            loadAccountInformationRequest.getBankAccess().getTanTransportTypes().put(BankApi.HBCI,
                extractTanTransportTypes(passport));
        }

        return LoadAccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(hbciAccounts)
            .build();
    }
}
