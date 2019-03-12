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

package de.adorsys.xs2a.model;

import java.util.Objects;
import java.util.UUID;

public abstract class XS2AUpdateRequest {
    private String authorisationId;
    private UUID requestId;
    private Object body;
    private String psuId;
    private String psuCorporateId;
    private String psuIpAddress;


    public String getAuthorisationId() {
        return authorisationId;
    }

    public void setAuthorisationId(String authorisationId) {
        this.authorisationId = authorisationId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getPsuId() {
        return psuId;
    }

    public void setPsuId(String psuId) {
        this.psuId = psuId;
    }

    public String getPsuCorporateId() {
        return psuCorporateId;
    }

    public void setPsuCorporateId(String psuCorporateId) {
        this.psuCorporateId = psuCorporateId;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }

    public void setPsuIpAddress(String psuIpAddress) {
        this.psuIpAddress = psuIpAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XS2AUpdateRequest that = (XS2AUpdateRequest) o;
        return Objects.equals(authorisationId, that.authorisationId) &&
                       Objects.equals(requestId, that.requestId) &&
                       Objects.equals(body, that.body) &&
                       Objects.equals(psuId, that.psuId) &&
                       Objects.equals(psuCorporateId, that.psuCorporateId) &&
                       Objects.equals(psuIpAddress, that.psuIpAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorisationId, requestId, body, psuId, psuCorporateId, psuIpAddress);
    }

    @Override
    public String toString() {
        return "XS2AUpdateRequest{" +
                       "authorisationId='" + authorisationId + '\'' +
                       ", requestId='" + requestId + '\'' +
                       ", body=" + body +
                       ", psuId='" + psuId + '\'' +
                       ", psuCorporateId='" + psuCorporateId + '\'' +
                       ", psuIpAddress='" + psuIpAddress + '\'' +
                       '}';
    }
}
