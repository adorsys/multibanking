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
import de.adorsys.psd2.client.model.BulkPaymentInitiationSctJson;
import de.adorsys.psd2.client.model.PaymentInitiationSctBulkElementJson;
import de.adorsys.xs2a.pis.PaymentInitiationBodyBuilder;
import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.BulkPayment;
import de.adorsys.multibanking.domain.SinglePayment;

import java.util.ArrayList;

public class SepaBulkPaymentInitiationBodyBuilder implements PaymentInitiationBodyBuilder<BulkPaymentInitiationSctJson> {
    @Override
    public BulkPaymentInitiationSctJson buildBody(AbstractScaTransaction transaction) {
        BulkPayment bulkPayment = (BulkPayment) transaction;
        BulkPaymentInitiationSctJson bulk = new BulkPaymentInitiationSctJson();
        AccountReference debtorAccountReference = new AccountReference();
        debtorAccountReference.setIban(bulkPayment.getDebtorBankAccount().getIban());
        bulk.setDebtorAccount(debtorAccountReference);
        ArrayList<PaymentInitiationSctBulkElementJson> payments = new ArrayList<>();
        bulk.setPayments(payments);

        for (SinglePayment payment : bulkPayment.getPayments()) {
            PaymentInitiationSctBulkElementJson bulkElementJson = new PaymentInitiationSctBulkElementJson();
            AccountReference creditorAccountReference = new AccountReference();
            creditorAccountReference.setIban(payment.getReceiverIban());

            Amount amount = new Amount();
            amount.setAmount(payment.getAmount().toString());
            amount.setCurrency(payment.getCurrency());

            bulkElementJson.setCreditorAccount(creditorAccountReference);
            bulkElementJson.setInstructedAmount(amount);
            bulkElementJson.setCreditorName(payment.getReceiver());
            bulkElementJson.setRemittanceInformationUnstructured(payment.getPurpose());
            payments.add(bulkElementJson);
        }
        return bulk;
    }
}
