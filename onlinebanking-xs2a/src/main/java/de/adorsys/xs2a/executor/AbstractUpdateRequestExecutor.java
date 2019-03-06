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

package de.adorsys.xs2a.executor;

import de.adorsys.psd2.client.model.TransactionAuthorisation;
import de.adorsys.xs2a.model.XS2AUpdateRequest;
import de.adorsys.xs2a.model.Xs2aTanSubmit;
import domain.request.SubmitAuthorizationCodeRequest;

import java.util.UUID;

import static de.adorsys.xs2a.XS2ABanking.PSU_IP_ADDRESS;

abstract class AbstractUpdateRequestExecutor<T extends XS2AUpdateRequest> implements UpdateRequestExecutor<T> {
    @Override
    public final T buildRequest(SubmitAuthorizationCodeRequest request) {
        Xs2aTanSubmit tanSubmit = (Xs2aTanSubmit) request.getTanSubmit();
        T updateRequest = createRequest(tanSubmit);
        updateRequest.setAuthorisationId(tanSubmit.getAuthorisationId());
        updateRequest.setPsuId(tanSubmit.getPsuId());
        updateRequest.setPsuCorporateId(tanSubmit.getPsuCorporateId());
        updateRequest.setPsuIpAddress(PSU_IP_ADDRESS);
        updateRequest.setRequestId(UUID.randomUUID());
        updateRequest.setBody(buildTransactionAuthorisation(request.getTan()));
        return updateRequest;
    }

    abstract T createRequest(Xs2aTanSubmit tanSubmit);

    private TransactionAuthorisation buildTransactionAuthorisation(String tan) {
        TransactionAuthorisation transactionAuthorisation = new TransactionAuthorisation();
        transactionAuthorisation.setScaAuthenticationData(tan);
        return transactionAuthorisation;
    }
}
