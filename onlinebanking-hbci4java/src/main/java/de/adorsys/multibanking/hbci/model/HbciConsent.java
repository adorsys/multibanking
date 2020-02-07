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

package de.adorsys.multibanking.hbci.model;

import de.adorsys.multibanking.domain.Credentials;
import de.adorsys.multibanking.domain.ScaStatus;
import de.adorsys.multibanking.domain.TanTransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kapott.hbci.manager.HBCIProduct;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HbciConsent {

    private HBCIProduct hbciProduct;
    private ScaStatus status;
    private Credentials credentials;
    private List<TanTransportType> tanMethodList;
    private TanTransportType selectedMethod;
    private String scaAuthenticationData;
    private Object hbciTanSubmit;
    private boolean withHktan = true; //ING hack, anoymous dialog & hktan for dialog not supported

    private String hbciSysId;
    private Map<String, String> hbciUpd;

    public void afterTransactionAuthorisation(ScaStatus scaStatus) {
        setHbciTanSubmit(null);
        setStatus(scaStatus);
        setScaAuthenticationData(null);
    }
}
