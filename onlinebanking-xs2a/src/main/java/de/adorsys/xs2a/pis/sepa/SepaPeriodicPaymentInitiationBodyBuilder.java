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

package de.adorsys.xs2a.pis.sepa;

import de.adorsys.psd2.client.model.AccountReference;
import de.adorsys.psd2.client.model.Amount;
import de.adorsys.psd2.client.model.PeriodicPaymentInitiationSctJson;
import de.adorsys.xs2a.pis.PaymentInitiationBodyBuilder;
import domain.AbstractScaTransaction;
import domain.FutureBulkPayment;
import domain.FutureSinglePayment;
import domain.SinglePayment;

public class SepaPeriodicPaymentInitiationBodyBuilder implements PaymentInitiationBodyBuilder<PeriodicPaymentInitiationSctJson> {
    @Override
    public PeriodicPaymentInitiationSctJson buildBody(AbstractScaTransaction transaction) {
        FutureSinglePayment paymentBodyObj = (FutureSinglePayment) transaction;
        PeriodicPaymentInitiationSctJson periodic = new PeriodicPaymentInitiationSctJson();
        AccountReference debtorAccountReference = new AccountReference();
        debtorAccountReference.setIban(paymentBodyObj.getDebtorBankAccount().getIban());

        AccountReference creditorAccountReference = new AccountReference();
        creditorAccountReference.setIban(paymentBodyObj.getReceiverIban());

        Amount amount = new Amount();
        amount.setAmount(paymentBodyObj.getAmount().toString());
        amount.setCurrency(paymentBodyObj.getCurrency());

        periodic.setDebtorAccount(debtorAccountReference);
        periodic.setCreditorAccount(creditorAccountReference);
        periodic.setInstructedAmount(amount);
        periodic.setCreditorName(paymentBodyObj.getReceiver());
        periodic.setRemittanceInformationUnstructured(paymentBodyObj.getPurpose());
        return periodic;
    }
}
