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

import de.adorsys.multibanking.bg.model.DayOfExecution;
import de.adorsys.multibanking.bg.model.PeriodicPaymentInitiationJson;
import de.adorsys.multibanking.domain.AbstractScaTransaction;
import de.adorsys.multibanking.domain.FutureSinglePayment;

import java.time.LocalDate;

public class SepaPeriodicPaymentInitiationBodyBuilder extends AbstractPaymentInitiationBodyBuilder<PeriodicPaymentInitiationJson> {
    @Override
    public PeriodicPaymentInitiationJson buildBody(AbstractScaTransaction transaction) {
        FutureSinglePayment paymentBodyObj = (FutureSinglePayment) transaction;

        PeriodicPaymentInitiationJson periodic = new PeriodicPaymentInitiationJson();
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
