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

package de.adorsys.multibanking.domain.exception;

public enum MultibankingError {

    HBCI_ERROR,
    INVALID_PAYMENT,
    INVALID_SCA_METHOD,
    INVALID_CONSENT,
    INVALID_PIN,
    INVALID_TAN,
    INVALID_ACCOUNT_REFERENCE,
    NO_CONSENT,
    INVALID_CONSENT_STATUS,
    BANK_NOT_SUPPORTED,
    BANKING_GATEWAY_ERROR,
    BOOKINGS_FORMAT_NOT_SUPPORTED,
    INTERNAL_ERROR;

}
