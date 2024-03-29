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

package de.adorsys.multibanking.hbci;

import de.adorsys.multibanking.domain.Bank;
import de.adorsys.multibanking.domain.Message;
import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.kapott.hbci.callback.AbstractHBCICallback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.domain.exception.MultibankingError.INVALID_PIN;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class HbciBpdUpdCallback extends AbstractHBCICallback {

    private final String bankCode;
    private final HbciBpdCacheHolder hbciBpdCacheHolder;
    private Map<String, String> upd;
    private String sysId;

    @SuppressWarnings("unchecked")
    @Override
    public void status(int statusTag, Object o) {
        if (statusTag == STATUS_INST_BPD_INIT_DONE) {
            hbciBpdCacheHolder.updateBpd(bankCode, (Map<String, String>) o);
        } else if (statusTag == STATUS_INIT_UPD_DONE) {
            this.upd = (Map<String, String>) o;
        }
    }

    @Override
    public void callback(int reason, List<String> messages, int datatype, StringBuilder retData) {
        if (reason == WRONG_PIN) {
            throw new MultibankingException(INVALID_PIN, messages.stream()
                .map(messageString -> {
                    if (StringUtils.equals(":", StringUtils.substring(messageString, 4, 5))) {
                        String hbciCode = StringUtils.substring(messageString, 0, 4);
                        String msg = StringUtils.substring(messageString, 5);
                        return new Message(hbciCode, null, null, msg, null);
                    } else {
                        return new Message(null, null, null, messageString, null);
                    }

                })
                .collect(Collectors.toList()));
        }
    }

    @Override
    public void status(int statusTag, Object[] o) {
        if (statusTag == STATUS_INIT_SYSID_DONE) {
            this.sysId = o[1].toString();
        }
    }

    public HbciConsent updateConsentUpd(HbciConsent consent) {
        Optional.ofNullable(upd).ifPresent(newUpd -> {
            consent.setHbciUpd(newUpd);
            consent.setUpdCacheUpdateTime(LocalDateTime.now());
            consent.setSysIdUpdUpdated(true);
        });
        Optional.ofNullable(sysId).ifPresent(newSysId -> {
            consent.setHbciSysId(newSysId);
            consent.setSysIdCacheUpdateTime(LocalDateTime.now());
            consent.setSysIdUpdUpdated(true);
        });

        return consent;
    }

    public static HbciBpdUpdCallback createCallback(Bank bank, HbciBpdCacheHolder hbciBpdCacheHolder) {
        String bankCode = Optional.ofNullable(bank.getBankApiBankCode())
            .orElse(bank.getBankCode());

        return new HbciBpdUpdCallback(bankCode, hbciBpdCacheHolder);
    }
}
