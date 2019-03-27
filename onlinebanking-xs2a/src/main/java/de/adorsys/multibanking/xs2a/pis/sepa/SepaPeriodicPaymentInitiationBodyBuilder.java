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

package de.adorsys.multibanking.xs2a.pis.sepa;

import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.FutureSinglePayment;
import de.adorsys.psd2.client.model.DayOfExecution;
import de.adorsys.psd2.client.model.PeriodicPaymentInitiationSctJson;

import java.time.LocalDate;

public class SepaPeriodicPaymentInitiationBodyBuilder extends AbstractPaymentInitiationBodyBuilder<PeriodicPaymentInitiationSctJson> {
    @Override
    public PeriodicPaymentInitiationSctJson buildBody(AbstractScaTransaction transaction) {
        FutureSinglePayment paymentBodyObj = (FutureSinglePayment) transaction;

        PeriodicPaymentInitiationSctJson periodic = new PeriodicPaymentInitiationSctJson();
        periodic.setDebtorAccount(buildDebtorAccountReference(paymentBodyObj));
        periodic.setCreditorAccount(buildCreditorAccountReference(paymentBodyObj));
        periodic.setInstructedAmount(buildAmount(paymentBodyObj));
        periodic.setCreditorName(paymentBodyObj.getReceiver());
        periodic.setRemittanceInformationUnstructured(paymentBodyObj.getPurpose());

        //todo: check if execution date was correct populated
        LocalDate executionDate = paymentBodyObj.getExecutionDate();
        periodic.setDayOfExecution(DayOfExecution.fromValue(String.valueOf(executionDate.getDayOfMonth())));
        return periodic;
    }
}
