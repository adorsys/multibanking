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

package de.adorsys.multibanking.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {

    ACCC("AcceptedSettlementCompletedCreditor", true), // Settlement on the creditor's account has been completed.
    ACCP("AcceptedCustomerProfile", false),  //Preceding check of technical validation was successful. Customer profile check was also successful
    ACSC("AcceptedSettlementCompleted", true),  //Settlement on the debtor's account has been completed. Usage : this can be used by the first agent to report to the debtor that the transaction has been completed. Warning : this status is provided for transaction status reasons, not for financial information. It can only be used after bilateral agreement"),
    ACSP("AcceptedSettlementInProcess", false),  //All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution
    ACTC("AcceptedTechnicalValidation", false),  //AuthenticationObject and syntactical and semantical validation are successful"),
    ACWC("AcceptedWithChange", false),  //Instruction is accepted but a change will be made, such as date or remittance not sent
    ACWP("AcceptedWithoutPosting", false),  //Payment instruction included in the credit transfer is accepted without being posted to the creditor customer’s account
    RCVD("Received", false),  //Payment initiation has been received by the receiving agent
    PDNG("Pending", false),  //Payment initiation or individual transaction included in the payment initiation is pending. Further checks and status update will be performed
    RJCT("Rejected", true),  //Payment initiation or individual transaction included in the payment initiation has been rejected
    CANC("Canceled", true), //Canceled
    ACFC("AcceptedFundsChecked", false), //Preceding check of technical validation and customer profile was successful and an automatic funds check was positive
    PATC("PartiallyAcceptedTechnicalCorrect", false); // The payment initiation needs multiple authentications, where some but not yet all have been performed. Syntactical and semantical validations are successful.

    private final String transactionStatus;
    private final boolean finalisedStatus;
}
