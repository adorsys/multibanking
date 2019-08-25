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

package de.adorsys.multibanking.domain.request;

import de.adorsys.multibanking.domain.Bank;
import de.adorsys.multibanking.domain.Credentials;
import de.adorsys.multibanking.domain.Product;
import lombok.Data;

import java.util.Map;

@Data
public abstract class AbstractRequest {

    private Object bankApiConsentData;
    private Bank bank;
    private Product hbciProduct;
    private Map<String, String> hbciBPD;
    private Map<String, String> hbciUPD;
    private String hbciSysId;

}
