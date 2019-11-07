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
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSEPAInfo;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class AccountInformationJob extends ScaRequiredJob<LoadAccounts, AccountInformationResponse> {

    private final TransactionRequest<LoadAccounts> loadAccountInformationRequest;

    private List<BankAccount> hbciAccounts;

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        if (!passport.jobSupported("SEPAInfo"))
            throw new MultibankingException(HBCI_ERROR, "SEPAInfo job not supported");

        return new GVSEPAInfo(passport);
    }

    @Override
    public List<AbstractHBCIJob> createAdditionalMessages(PinTanPassport passport) {
        return Collections.emptyList();
    }

    @Override
    TransactionRequest<LoadAccounts> getTransactionRequest() {
        return loadAccountInformationRequest;
    }

    @Override
    String getHbciJobName(AbstractTransaction.TransactionType transactionType) {
        return GVSEPAInfo.getLowlevelName();
    }

    @Override
    public String orderIdFromJobResult(HBCIJobResult jobResult) {
        return null;
    }

    @Override
    public AccountInformationResponse createJobResponse(PinTanPassport passport) {
        loadAccountInformationRequest.getBankAccess().setBankName(passport.getInstName());

        hbciAccounts = new ArrayList<>();
        for (Konto konto : passport.getAccounts()) {
            BankAccount bankAccount = accountStatementMapper.toBankAccount(konto);
            bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
            bankAccount.bankName(loadAccountInformationRequest.getBankAccess().getBankName());
            hbciAccounts.add(bankAccount);
        }

        return AccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(hbciAccounts)
            .build();
    }
}
