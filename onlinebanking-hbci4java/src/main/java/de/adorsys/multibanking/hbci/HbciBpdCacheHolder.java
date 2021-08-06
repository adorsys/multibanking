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

import de.adorsys.multibanking.domain.request.AbstractRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.kapott.hbci.passport.PinTanPassport.BPD_KEY_LASTUPDATE;

@RequiredArgsConstructor
public class HbciBpdCacheHolder {

    private final long bpdMaxAgeMillis;

    private final Map<String, Map<String, String>> bpdCache = new ConcurrentHashMap<>();

    public Map<String, String> getBpd(AbstractRequest request) {
        String bankCode = Optional.ofNullable(request.getBank().getBankApiBankCode())
            .orElse(request.getBank().getBankCode());

        return getBpd(bankCode);
    }

    public Map<String, String> getBpd(String bankCode) {
        return Optional.ofNullable(bpdCache.get(bankCode))
            .map(bpdMap -> {
                long bpdLastUpdate = Long.parseLong(bpdMap.get(BPD_KEY_LASTUPDATE));
                if ((System.currentTimeMillis() - bpdLastUpdate) < bpdMaxAgeMillis) {
                    return bpdMap;
                }
                return null;
            })
            .orElse(null);

    }

    public void updateBpd(String bankCode, Map<String, String> bpd) {
        bpdCache.put(bankCode, bpd);
    }
}
