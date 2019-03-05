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

public class Xs2aTanSubmit {
    private String bankingUrl;
    private String transactionId;
    private String authorisationId;
    private String psuId;
    private String psuCorporateId;

    public Xs2aTanSubmit() {
    }

    public Xs2aTanSubmit(String bankingUrl, String transactionId, String authorisationId, String psuId, String psuCorporateId) {
        this.bankingUrl = bankingUrl;
        this.transactionId = transactionId;
        this.authorisationId = authorisationId;
        this.psuId = psuId;
        this.psuCorporateId = psuCorporateId;
    }

    public String getBankingUrl() {
        return bankingUrl;
    }

    public void setBankingUrl(String bankingUrl) {
        this.bankingUrl = bankingUrl;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public void setAuthorisationId(String authorisationId) {
        this.authorisationId = authorisationId;
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
}
