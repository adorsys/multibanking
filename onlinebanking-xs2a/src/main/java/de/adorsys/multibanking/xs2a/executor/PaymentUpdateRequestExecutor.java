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

import de.adorsys.multibanking.xs2a.model.PaymentXS2AUpdateRequest;
import de.adorsys.multibanking.xs2a.model.Xs2aTanSubmit;
import de.adorsys.psd2.client.ApiClient;
import de.adorsys.psd2.client.ApiException;
import de.adorsys.psd2.client.api.PaymentInitiationServicePisApi;

import static de.adorsys.multibanking.xs2a.XS2ABanking.SEPA_CREDIT_TRANSFERS;
import static de.adorsys.multibanking.xs2a.XS2ABanking.SINGLE_PAYMENT_SERVICE;

public class PaymentUpdateRequestExecutor extends AbstractUpdateRequestExecutor<PaymentXS2AUpdateRequest> {
    @Override
    public String execute(PaymentXS2AUpdateRequest req, ApiClient apiClient) throws ApiException {
        PaymentInitiationServicePisApi pis = createApiClient(apiClient);
        pis.updatePaymentPsuData(req.getService(), req.getProduct(), req.getPaymentId(), req.getAuthorisationId(),
                req.getRequestId(), req.getBody(), null, null, null,
                req.getPsuId(), null, req.getPsuCorporateId(), null,
                req.getPsuIpAddress(), null, null, null,
                null, null, null, null,
                null, null);
        return req.getPaymentId();
    }

    @Override
    PaymentXS2AUpdateRequest createRequest(Xs2aTanSubmit tanSubmit) {
        PaymentXS2AUpdateRequest request = new PaymentXS2AUpdateRequest();
        request.setPaymentId(tanSubmit.getTransactionId());
        request.setService(SINGLE_PAYMENT_SERVICE);
        request.setProduct(SEPA_CREDIT_TRANSFERS);
        return request;
    }

    PaymentInitiationServicePisApi createApiClient(ApiClient apiClient) {
        return new PaymentInitiationServicePisApi(apiClient);
    }
}