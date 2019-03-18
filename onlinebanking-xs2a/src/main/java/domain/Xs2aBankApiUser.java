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

package domain;

import java.util.HashMap;

public class Xs2aBankApiUser extends BankApiUser {

    public Xs2aBankApiUser(String consentId) {
        this();
        setConsentId(consentId);
    }

    public Xs2aBankApiUser() {
        setBankApi(BankApi.XS2A);
    }

    private static final String CONSENT_ID = "CONSENT_ID";

    public String getConsentId() {
        return getProperties().get(CONSENT_ID);
    }

    public void setConsentId(String consentId) {
        if (getProperties() == null) {
            setProperties(new HashMap<>());
        }
        getProperties().put(CONSENT_ID, consentId);
    }
}
