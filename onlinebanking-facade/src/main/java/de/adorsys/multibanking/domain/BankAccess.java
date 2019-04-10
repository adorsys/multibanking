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

package de.adorsys.multibanking.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexg on 07.02.17.
 */
@Data
@ApiModel(description = "BankAccess account information", value = "BankAccess")
public class BankAccess {

    @ApiModelProperty(value = "Bank name", example = "Deutsche Bank")
    private String bankName;
    @ApiModelProperty(value = "SCA consent")
    private Consent allAcountsConsent;
    @ApiModelProperty(value = "Bank login name", required = true, example = "l.name")
    private String bankLogin;
    @ApiModelProperty(value = "2nd bank login name", example = "1234567890")
    private String bankLogin2;
    @ApiModelProperty(value = "Bank code", required = true, example = "76070024")
    private String bankCode;
    @ApiModelProperty(value = "IBAN", required = true, example = "DE51250400903312345678")
    private String iban;
    @ApiModelProperty(value = "Supported tan transport types", example = "iTAN")
    private Map<BankApi, List<TanTransportType>> tanTransportTypes = new EnumMap<>(BankApi.class);
    @ApiModelProperty(hidden = true)
    private String hbciPassportState;
    @ApiModelProperty(hidden = true)
    private Map<BankApi, String> externalIdMap = new EnumMap<>(BankApi.class);

    public void externalId(BankApi bankApi, String externalId) {
        externalIdMap.put(bankApi, externalId);
    }

}
