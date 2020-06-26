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
import de.adorsys.multibanking.domain.request.TransactionRequestFactory;
import de.adorsys.multibanking.domain.response.AccountInformationResponse;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.LoadAccounts;
import de.adorsys.multibanking.domain.transaction.LoadBalances;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import de.adorsys.multibanking.hbci.model.HbciTanSubmit;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV.GVSEPAInfo;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.structures.Konto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.HBCI_ERROR;

@Slf4j
public class AccountInformationJob extends ScaAwareJob<LoadAccounts, AccountInformationResponse> {

    private final TransactionRequest<LoadAccounts> loadAccountInformationRequest;
    private AbstractHBCIJob sepaInfoJob;

    public AccountInformationJob(TransactionRequest<LoadAccounts> loadAccountInformationRequest) {
        this.loadAccountInformationRequest = loadAccountInformationRequest;
        if (loadAccountInformationRequest.getTransaction().isWithBalances()) {
            ((HbciConsent) loadAccountInformationRequest.getBankApiConsentData()).setCloseDialog(false);
        }
    }

    @Override
    public AbstractHBCIJob createJobMessage(PinTanPassport passport) {
        if (!passport.jobSupported("SEPAInfo"))
            throw new MultibankingException(HBCI_ERROR, "SEPAInfo job not supported");

        sepaInfoJob = new GVSEPAInfo(passport);
        return sepaInfoJob;
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
    public AccountInformationResponse createJobResponse(PinTanPassport passport, HbciTanSubmit tanSubmit) {
        loadAccountInformationRequest.getBankAccess().setBankName(passport.getInstName());

        List<Konto> hbciAccounts = passport.getAccounts();
        List<BankAccount> result = hbciAccounts.stream()
            .map(konto -> {
                BankAccount bankAccount = accountStatementMapper.toBankAccount(konto);
                bankAccount.externalId(BankApi.HBCI, UUID.randomUUID().toString());
                bankAccount.bankName(loadAccountInformationRequest.getBankAccess().getBankName());

                if (sepaInfoJob.getJobResult().isOK() && loadAccountInformationRequest.getTransaction().isWithBalances() && konto.allowedGVs.contains("HKSAL")) {
                    LoadBalances loadBalances = new LoadBalances();
                    loadBalances.setPsuAccount(bankAccount);

                    TransactionRequest<LoadBalances> loadBalancesRequest =
                        TransactionRequestFactory.create(loadBalances, null, loadAccountInformationRequest.getBankAccess(),
                            loadAccountInformationRequest.getBank(), loadAccountInformationRequest.getBankApiConsentData());
                    new LoadBalancesJob(loadBalancesRequest).execute(null, this.dialog);
                }

                return bankAccount;
            })
            .collect(Collectors.toList());

        //finally close dialog
        if (sepaInfoJob.getJobResult().isOK() && loadAccountInformationRequest.getTransaction().isWithBalances()) {
            this.dialog.dialogEnd();
        }

        return AccountInformationResponse.builder()
            .bankAccess(loadAccountInformationRequest.getBankAccess())
            .bankAccounts(result)
            .build();
    }
}
