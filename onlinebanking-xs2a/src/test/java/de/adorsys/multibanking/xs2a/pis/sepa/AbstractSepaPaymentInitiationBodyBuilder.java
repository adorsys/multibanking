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

import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.SinglePayment;
import de.adorsys.multibanking.xs2a.pis.PaymentProductType;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

import java.math.BigDecimal;

abstract class AbstractSepaPaymentInitiationBodyBuilder {
    static final String IBAN = Iban.random(CountryCode.DE).toString();
    static final String CREDITOR_NAME = "creditor name";
    static final String INFORMATION = "information";
    static final String CURRENCY = "UAH";
    static final int AMOUNT_VALUE = 1;

    BankAccount buildBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setIban(IBAN);
        return bankAccount;
    }

    SinglePayment buildSinglePayment() {
        SinglePayment payment = new SinglePayment();
        payment.setDebtorBankAccount(buildBankAccount());
        payment.setProduct(PaymentProductType.SEPA.getType());
        payment.setAmount(new BigDecimal(AMOUNT_VALUE));
        payment.setReceiver(CREDITOR_NAME);
        payment.setPurpose(INFORMATION);
        payment.setReceiverIban(IBAN);
        payment.setCurrency(CURRENCY);
        return payment;
    }
}
