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

package de.adorsys.multibanking.bg.pis.sepa;

import de.adorsys.multibanking.bg.model.BulkPaymentInitiationSctJson;
import de.adorsys.multibanking.bg.model.PaymentInitiationSctBulkElementJson;
import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.BulkPayment;
import de.adorsys.multibanking.domain.SinglePayment;


public class SepaBulkPaymentInitiationBodyBuilder extends AbstractPaymentInitiationBodyBuilder<BulkPaymentInitiationSctJson> {
    @Override
    public BulkPaymentInitiationSctJson buildBody(AbstractScaTransaction transaction) {
        BulkPayment bulkPayment = (BulkPayment) transaction;
        BulkPaymentInitiationSctJson bulk = new BulkPaymentInitiationSctJson();
        bulk.setDebtorAccount(buildDebtorAccountReference(transaction));

        for (SinglePayment payment : bulkPayment.getPayments()) {
            PaymentInitiationSctBulkElementJson bulkElementJson = new PaymentInitiationSctBulkElementJson();
            bulkElementJson.setCreditorAccount(buildCreditorAccountReference(payment));
            bulkElementJson.setInstructedAmount(buildAmount(payment));
            bulkElementJson.setCreditorName(payment.getReceiver());
            bulkElementJson.setRemittanceInformationUnstructured(payment.getPurpose());
            bulk.addPaymentsItem(bulkElementJson);
        }
        return bulk;
    }
}
