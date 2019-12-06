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

package de.adorsys.multibanking.domain.spi;

import de.adorsys.multibanking.domain.Consent;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.request.SelectPsuAuthenticationMethodRequest;
import de.adorsys.multibanking.domain.request.TransactionAuthorisationRequest;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.request.UpdatePsuAuthenticationRequest;
import de.adorsys.multibanking.domain.response.AuthorisationCodeResponse;
import de.adorsys.multibanking.domain.response.CreateConsentResponse;
import de.adorsys.multibanking.domain.response.PaymentStatusResponse;
import de.adorsys.multibanking.domain.response.UpdateAuthResponse;
import de.adorsys.multibanking.domain.transaction.PaymentStatusReqest;

public interface StrongCustomerAuthorisable {

    CreateConsentResponse createConsent(Consent consentTemplate, boolean redirectPreferred, String tppRedirectUri,
                                        Object bankApiConsentData);

    Consent getConsent(String consentId);

    UpdateAuthResponse updatePsuAuthentication(UpdatePsuAuthenticationRequest updatePsuAuthentication);

    UpdateAuthResponse authorizeConsent(TransactionAuthorisationRequest transactionAuthorisationRequest);

    UpdateAuthResponse selectPsuAuthenticationMethod(SelectPsuAuthenticationMethodRequest selectPsuAuthenticationMethod);

    void revokeConsent(String consentId);

    UpdateAuthResponse getAuthorisationStatus(String consentId, String authorisationId, Object bankApiConsentData);

    /**
     * @param consentId             consent id
     * @param authorisationId       authorisation id
     * @param expectedConsentStatus consent status
     * @param bankApiConsentData    bank api specific consent data
     */
    void validateConsent(String consentId, String authorisationId, ScaStatus expectedConsentStatus,
                         Object bankApiConsentData);

    void afterExecute(Object bankApiConsentData, AuthorisationCodeResponse authorisationCodeResponse);

    void submitAuthorisationCode(Object bankApiConsentData, String authorisationCode);

    PaymentStatusResponse getPaymentStatus(TransactionRequest<PaymentStatusReqest> request);
}
