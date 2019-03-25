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

package de.adorsys.multibanking.xs2a.executor;

import de.adorsys.multibanking.xs2a.model.ConsentXS2AUpdateRequest;
import de.adorsys.multibanking.xs2a.model.Xs2aTanSubmit;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.AccountInformationServiceAisApi;

public class ConsentUpdateRequestExecutor extends AbstractUpdateRequestExecutor<ConsentXS2AUpdateRequest> {
    @Override
    public String execute(ConsentXS2AUpdateRequest req, ApiClient apiClient) throws ApiException {
        AccountInformationServiceAisApi ais = createAisClient(apiClient);
        String consentId = req.getConsentId();
        ais.updateConsentsPsuData(consentId, req.getAuthorisationId(), req.getRequestId(), req.getBody(), null,
                null, null, req.getPsuId(), null,
                req.getPsuCorporateId(), null, req.getPsuIpAddress(), null,
                null, null, null, null,
                null, null, null, null);
        return consentId;
    }

    @Override
    ConsentXS2AUpdateRequest createRequest(Xs2aTanSubmit tanSubmit) {
        ConsentXS2AUpdateRequest request = new ConsentXS2AUpdateRequest();
        request.setConsentId(tanSubmit.getTransactionId());
        return request;
    }

    AccountInformationServiceAisApi createAisClient(ApiClient apiClient) {
        return new AccountInformationServiceAisApi(apiClient);
    }
}